import 'dart:convert';
import 'package:http/http.dart' as http;
import '../auth/token_storage.dart';
import 'case_conversion.dart';

/// Erreur API normalisee (enveloppe { erreur: { code, message } }, Volume 6 §2.3).
class ApiException implements Exception {
  final String code;
  final String message;
  ApiException(this.code, this.message);
  @override
  String toString() => message;
}

/// Client HTTP unique de l'application, meme discipline que lib/api.ts cote web :
/// jeton attache automatiquement, conversion camelCase <-> snake_case aux
/// frontieres, rafraichissement automatique sur 401.
class ApiClient {
  final String baseUrl;
  final TokenStorage tokenStorage;

  ApiClient({required this.baseUrl, required this.tokenStorage});

  Future<dynamic> get(String chemin, {Map<String, String>? params}) => _requete('GET', chemin, params: params);
  Future<dynamic> post(String chemin, {Map<String, dynamic>? corps}) => _requete('POST', chemin, corps: corps);
  Future<dynamic> patch(String chemin, {Map<String, dynamic>? corps}) => _requete('PATCH', chemin, corps: corps);

  Future<dynamic> _requete(String methode, String chemin, {Map<String, String>? params, Map<String, dynamic>? corps, bool dejaRafraichi = false}) async {
    final jeton = await tokenStorage.lireJeton();
    var uri = Uri.parse('$baseUrl/api/v1$chemin');
    if (params != null) uri = uri.replace(queryParameters: params);

    final entetes = {
      'Content-Type': 'application/json',
      if (jeton != null) 'Authorization': 'Bearer $jeton',
    };
    final corpsJson = corps != null ? jsonEncode(versSnakeCase(corps)) : null;

    final reponse = await _envoyer(methode, uri, entetes, corpsJson);

    if (reponse.statusCode == 401 && !dejaRafraichi && !chemin.startsWith('/auth/')) {
      final succes = await _tenterRafraichissement();
      if (succes) return _requete(methode, chemin, params: params, corps: corps, dejaRafraichi: true);
    }

    if (reponse.statusCode >= 400) {
      final donnees = reponse.body.isNotEmpty ? jsonDecode(reponse.body) : {};
      final erreur = donnees['erreur'] ?? {};
      throw ApiException(erreur['code'] ?? 'ERREUR_INCONNUE', erreur['message'] ?? 'Une erreur est survenue.');
    }

    if (reponse.body.isEmpty) return null;
    return versCamelCase(jsonDecode(reponse.body));
  }

  Future<http.Response> _envoyer(String methode, Uri uri, Map<String, String> entetes, String? corps) {
    switch (methode) {
      case 'POST': return http.post(uri, headers: entetes, body: corps);
      case 'PATCH': return http.patch(uri, headers: entetes, body: corps);
      default: return http.get(uri, headers: entetes);
    }
  }

  Future<bool> _tenterRafraichissement() async {
    final jetonRafraichissement = await tokenStorage.lireJetonRafraichissement();
    if (jetonRafraichissement == null) return false;
    try {
      final reponse = await http.post(
        Uri.parse('$baseUrl/api/v1/auth/refresh'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(versSnakeCase({'jetonRafraichissement': jetonRafraichissement})),
      );
      if (reponse.statusCode != 200) {
        await tokenStorage.effacer();
        return false;
      }
      final donnees = versCamelCase(jsonDecode(reponse.body));
      await tokenStorage.enregistrer(jeton: donnees['jeton'], jetonRafraichissement: donnees['jetonRafraichissement']);
      return true;
    } catch (_) {
      return false;
    }
  }
}
