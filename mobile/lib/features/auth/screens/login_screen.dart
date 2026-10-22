import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/auth/auth_provider.dart';
import '../../../core/api/api_client.dart';
import '../../dashboard/screens/dashboard_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});
  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _emailController = TextEditingController();
  final _motDePasseController = TextEditingController();
  final _codeController = TextEditingController();
  String? _jetonDeuxFaEnAttente;
  bool _enCours = false;
  String? _erreur;

  Future<void> _seConnecter() async {
    setState(() { _enCours = true; _erreur = null; });
    final auth = context.read<AuthProvider>();
    try {
      final jetonDeuxFa = await auth.connecter(_emailController.text.trim(), _motDePasseController.text);
      if (jetonDeuxFa != null) {
        setState(() => _jetonDeuxFaEnAttente = jetonDeuxFa);
      } else if (mounted) {
        Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const DashboardScreen()));
      }
    } on ApiException catch (e) {
      setState(() => _erreur = e.message);
    } finally {
      setState(() => _enCours = false);
    }
  }

  Future<void> _verifierCode() async {
    setState(() { _enCours = true; _erreur = null; });
    final auth = context.read<AuthProvider>();
    try {
      await auth.verifierDeuxFa(_jetonDeuxFaEnAttente!, _codeController.text.trim());
      if (mounted) Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const DashboardScreen()));
    } on ApiException catch (e) {
      setState(() => _erreur = e.message);
    } finally {
      setState(() => _enCours = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final etapeDeuxFa = _jetonDeuxFaEnAttente != null;
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Icon(Icons.school, size: 48),
                const SizedBox(height: 12),
                Text(etapeDeuxFa ? 'Vérification en deux étapes' : 'Connexion',
                    style: Theme.of(context).textTheme.headlineSmall, textAlign: TextAlign.center),
                const SizedBox(height: 24),
                if (!etapeDeuxFa) ...[
                  TextField(controller: _emailController, decoration: const InputDecoration(labelText: 'Email', border: OutlineInputBorder()),
                      keyboardType: TextInputType.emailAddress),
                  const SizedBox(height: 12),
                  TextField(controller: _motDePasseController, decoration: const InputDecoration(labelText: 'Mot de passe', border: OutlineInputBorder()),
                      obscureText: true),
                ] else
                  TextField(controller: _codeController, decoration: const InputDecoration(labelText: 'Code à 6 chiffres', border: OutlineInputBorder()),
                      keyboardType: TextInputType.number, textAlign: TextAlign.center),
                if (_erreur != null) Padding(padding: const EdgeInsets.only(top: 12), child: Text(_erreur!, style: const TextStyle(color: Colors.red))),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: _enCours ? null : (etapeDeuxFa ? _verifierCode : _seConnecter),
                  child: _enCours ? const CircularProgressIndicator() : Text(etapeDeuxFa ? 'Vérifier' : 'Se connecter'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
