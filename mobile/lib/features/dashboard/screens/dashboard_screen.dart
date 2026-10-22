import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/auth/auth_provider.dart';
import '../../../core/api/api_client.dart';
import '../../auth/screens/login_screen.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});
  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  Map<String, dynamic>? _indicateurs;

  @override
  void initState() {
    super.initState();
    _charger();
  }

  Future<void> _charger() async {
    final api = context.read<ApiClient>();
    try {
      final donnees = await api.get('/dashboard/indicateurs');
      if (mounted) setState(() => _indicateurs = donnees);
    } catch (_) {
      // Reste affiche avec des "—" en cas d'echec reseau, coherent avec le comportement web.
    }
  }

  @override
  Widget build(BuildContext context) {
    final utilisateur = context.watch<AuthProvider>().utilisateur;

    return Scaffold(
      appBar: AppBar(
        title: Text(utilisateur != null ? '${utilisateur.prenom} ${utilisateur.nom}' : 'EDUCORE'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await context.read<AuthProvider>().deconnecter();
              if (context.mounted) Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const LoginScreen()));
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _charger,
        child: GridView.count(
          padding: const EdgeInsets.all(16),
          crossAxisCount: 2,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 1.3,
          children: [
            _carteIndicateur('Élèves inscrits', _indicateurs?['elevesInscrits']?.toString(), Icons.school),
            _carteIndicateur('Présence du jour', _indicateurs?['tauxPresenceDuJour'] != null ? '${_indicateurs!['tauxPresenceDuJour']}%' : null, Icons.check_circle_outline),
            _carteIndicateur('Paiements du mois', _indicateurs?['paiementsDuMois'] != null ? '${_indicateurs!['paiementsDuMois']} XAF' : null, Icons.payments_outlined),
            _carteIndicateur('Personnel actif', _indicateurs?['personnelActif']?.toString(), Icons.people_outline),
          ],
        ),
      ),
    );
  }

  Widget _carteIndicateur(String libelle, String? valeur, IconData icone) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icone, color: Theme.of(context).colorScheme.primary),
            const Spacer(),
            Text(valeur ?? '—', style: Theme.of(context).textTheme.headlineSmall),
            Text(libelle, style: Theme.of(context).textTheme.bodySmall),
          ],
        ),
      ),
    );
  }
}
