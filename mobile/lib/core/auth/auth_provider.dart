import 'package:flutter/foundation.dart';
import '../api/api_client.dart';
import '../auth/token_storage.dart';

class UtilisateurProfil {
  final int id;
  final String nom;
  final String prenom;
  final String email;
  final String rolePrincipal;
  final bool doitChangerMotDePasse;

  UtilisateurProfil({required this.id, required this.nom, required this.prenom, required this.email,
      required this.rolePrincipal, required this.doitChangerMotDePasse});

  factory UtilisateurProfil.depuisJson(Map<String, dynamic> json) => UtilisateurProfil(
      id: json['id'], nom: json['nom'], prenom: json['prenom'], email: json['email'],
      rolePrincipal: json['rolePrincipal'], doitChangerMotDePasse: json['doitChangerMotDePasse'] ?? false);
}

/// Meme logique que useAuth.ts cote web : connexion en deux temps si le 2FA
/// est actif (voir AuthProvider.connecter -> deuxFaRequis).
class AuthProvider extends ChangeNotifier {
  final ApiClient apiClient;
  final TokenStorage tokenStorage;

  UtilisateurProfil? utilisateur;
  bool chargement = true;

  AuthProvider({required this.apiClient, required this.tokenStorage}) {
    _hydrater();
  }

  Future<void> _hydrater() async {
    final jeton = await tokenStorage.lireJeton();
    if (jeton == null) {
      chargement = false;
      notifyListeners();
      return;
    }
    try {
      final profil = await apiClient.get('/auth/me');
      utilisateur = UtilisateurProfil.depuisJson(profil);
    } catch (_) {
      await tokenStorage.effacer();
    }
    chargement = false;
    notifyListeners();
  }

  /// Retourne le jetonDeuxFa si le compte a le 2FA actif (voir PageConnexion.tsx cote web pour le meme flux), sinon null.
  Future<String?> connecter(String email, String motDePasse) async {
    final reponse = await apiClient.post('/auth/login', corps: {'email': email, 'motDePasse': motDePasse});
    if (reponse['deuxFaRequis'] == true) return reponse['jetonDeuxFa'];

    await tokenStorage.enregistrer(jeton: reponse['jeton'], jetonRafraichissement: reponse['jetonRafraichissement']);
    utilisateur = UtilisateurProfil.depuisJson(reponse['utilisateur']);
    notifyListeners();
    return null;
  }

  Future<void> verifierDeuxFa(String jetonDeuxFa, String code) async {
    final reponse = await apiClient.post('/auth/2fa/verifier', corps: {'jetonDeuxFa': jetonDeuxFa, 'code': code});
    await tokenStorage.enregistrer(jeton: reponse['jeton'], jetonRafraichissement: reponse['jetonRafraichissement']);
    utilisateur = UtilisateurProfil.depuisJson(reponse['utilisateur']);
    notifyListeners();
  }

  Future<void> deconnecter() async {
    final jetonRafraichissement = await tokenStorage.lireJetonRafraichissement();
    if (jetonRafraichissement != null) {
      try { await apiClient.post('/auth/logout', corps: {'jetonRafraichissement': jetonRafraichissement}); } catch (_) {}
    }
    await tokenStorage.effacer();
    utilisateur = null;
    notifyListeners();
  }
}
