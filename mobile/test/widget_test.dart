// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';

import 'package:educore_mobile/core/auth/auth_provider.dart';

void main() {
  test('deserialize un utilisateur depuis la reponse API', () {
    final utilisateur = UtilisateurProfil.depuisJson({
      'id': 12,
      'nom': 'Diallo',
      'prenom': 'Aminata',
      'email': 'aminata@example.com',
      'rolePrincipal': 'ELEVE',
    });

    expect(utilisateur.id, 12);
    expect(utilisateur.nom, 'Diallo');
    expect(utilisateur.prenom, 'Aminata');
    expect(utilisateur.email, 'aminata@example.com');
    expect(utilisateur.rolePrincipal, 'ELEVE');
    expect(utilisateur.doitChangerMotDePasse, isFalse);
  });
}
