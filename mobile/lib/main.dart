import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/api/api_client.dart';
import 'core/auth/auth_provider.dart';
import 'core/auth/token_storage.dart';
import 'core/theme/educore_theme.dart';
import 'features/auth/screens/login_screen.dart';
import 'features/dashboard/screens/dashboard_screen.dart';

/// URL de l'API - conforme au Volume 6 : /api/v1 est ajoute par ApiClient.
/// En production, injecte via --dart-define=API_BASE_URL=https://api.educore.africa.
const String _apiBaseUrl = String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080');

void main() {
  final tokenStorage = TokenStorage();
  final apiClient = ApiClient(baseUrl: _apiBaseUrl, tokenStorage: tokenStorage);

  runApp(MultiProvider(
    providers: [
      Provider<ApiClient>.value(value: apiClient),
      ChangeNotifierProvider(create: (_) => AuthProvider(apiClient: apiClient, tokenStorage: tokenStorage)),
    ],
    child: const EducoreApp(),
  ));
}

class EducoreApp extends StatelessWidget {
  const EducoreApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'EDUCORE',
      debugShowCheckedModeBanner: false,
      theme: EducoreTheme.clair(),
      darkTheme: EducoreTheme.sombre(),
      home: const _EcranDemarrage(),
    );
  }
}

/// Attend l'hydratation silencieuse (jeton deja present ?) avant de choisir
/// l'ecran de depart - meme logique que App.tsx cote web.
class _EcranDemarrage extends StatelessWidget {
  const _EcranDemarrage();

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    if (auth.chargement) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    return auth.utilisateur != null ? const DashboardScreen() : const LoginScreen();
  }
}
