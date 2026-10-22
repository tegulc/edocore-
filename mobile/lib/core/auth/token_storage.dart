import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Stockage chiffre des jetons JWT (Keychain iOS / Keystore Android via
/// flutter_secure_storage), jamais SharedPreferences en clair - coherent
/// avec la Regle 06 posee cote backend (Phase 1) sur le traitement des jetons.
class TokenStorage {
  static const _storage = FlutterSecureStorage();
  static const _cleAcces = 'educore_access_token';
  static const _cleRafraichissement = 'educore_refresh_token';

  Future<void> enregistrer({required String jeton, required String jetonRafraichissement}) async {
    await _storage.write(key: _cleAcces, value: jeton);
    await _storage.write(key: _cleRafraichissement, value: jetonRafraichissement);
  }

  Future<String?> lireJeton() => _storage.read(key: _cleAcces);
  Future<String?> lireJetonRafraichissement() => _storage.read(key: _cleRafraichissement);

  Future<void> effacer() async {
    await _storage.delete(key: _cleAcces);
    await _storage.delete(key: _cleRafraichissement);
  }
}
