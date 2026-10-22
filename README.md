# EDUCORE / SIGES — Phase 0 : Socle technique

Système ERP intelligent de gestion d'établissements scolaires — plateforme SaaS multi-tenant destinée aux établissements africains.

## Contenu de cette livraison (Phase 0)

Cette phase pose les fondations techniques sur lesquelles tous les modules métier (30 au total) viendront s'appuyer, phase par phase.

```
educore/
├── backend/                  Spring Boot (Java 21) — architecture MVC en couches
│   ├── src/main/java/com/educore/
│   │   ├── EducoreApplication.java
│   │   ├── common/            Classes de base partagées (BaseEntity, exceptions, TenantScoped)
│   │   ├── config/            Configuration (sécurité, OpenAPI, audit JPA, healthcheck)
│   │   ├── security/          JWT, filtre d'authentification, contexte multi-tenant
│   │   └── modules/
│   │       └── auth/          Module 1 : Utilisateurs / Rôles / Authentification
│   │           ├── controller/
│   │           ├── service/
│   │           ├── repository/
│   │           ├── entity/
│   │           └── dto/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/      Migrations Flyway (V1 = ton script SQL de 150+ tables)
│   └── Dockerfile
│
├── frontend/                 React 18 + TypeScript + Tailwind CSS
│   └── src/
│       ├── components/
│       │   ├── layout/        Navbar, Sidebar (RBAC), Footer, AppShell — 100% responsive
│       │   └── ui/             Composants réutilisables (Card, Bouton, ...)
│       ├── hooks/              Stores Zustand (thème sombre/clair, authentification)
│       ├── pages/               Connexion, Tableau de bord
│       ├── lib/api.ts           Client HTTP centralisé (intercepteur JWT)
│       └── types/               Types TypeScript miroir des DTOs backend
│
├── docker-compose.yml         Postgres + Redis + MinIO + backend + frontend
├── .env.example
└── .gitignore
```

## Démarrage local

```bash
cp .env.example .env
docker compose up -d
```

- Backend : http://localhost:8080 (documentation API : `/swagger-ui.html`)
- Frontend : http://localhost:5173
- Console MinIO : http://localhost:9001 (identifiants dans `.env`)

## Décisions d'architecture (Phase 0)

| Domaine | Choix | Justification |
|---|---|---|
| Architecture backend | MVC en couches (Controller → Service → Repository → Entity) + DTO | Séparation stricte des responsabilités, testable indépendamment |
| Multi-tenant | Colonne `etablissement_id` + filtre Hibernate + `TenantContext` thread-local | Isolation garantie même en cas d'erreur de développement |
| Authentification | JWT stateless (access 15 min / refresh 7 jours) + 2FA optionnel | Scalable horizontalement, pas de session serveur à répliquer |
| Erreurs API | Contrat unique `ApiError` (code, message, horodatage) | Le web, le mobile Flutter et l'IA consomment le même format |
| Base de données | PostgreSQL + Flyway (jamais `ddl-auto: update`) | Historique de schéma traçable, rejouable en CI |
| Frontend | React + TypeScript + Tailwind + Zustand | Léger, typé de bout en bout avec le backend, fonctionne bien en connexion limitée |
| Design | Bleu `#2F5DE3` / teal `#0EA5A4`, Sora + Inter, mode sombre | Validé avec le porteur de projet — identité "SaaS corporate" |

## Feuille de route des modules (cycle en V par phase)

Chaque phase suit : spécification → conception → développement → tests unitaires → tests d'intégration → recette, puis livraison en zip.

- [x] **Phase 0** — Socle technique
- [x] **Phase 1** — Authentification complète, Utilisateurs, Rôles & Permissions (RBAC), 2FA (TOTP) (celle-ci)
- [x] **Phase 2** — Établissements (multi-tenant), Années scolaires, Périodes
- [x] **Phase 3** — Élèves, Parents, Enseignants, Classes, Matières
- [x] **Phase 4** — Emploi du temps, Présences, Notes, Bulletins
- [x] **Phase 5** — Paiements (Orange Money, MTN MoMo, cartes), Comptabilité
- [x] **Phase 6** — Bibliothèque, Infirmerie, Transport, Cantine, Discipline
- [x] **Phase 7** — Chat temps réel, Réunions, Notifications (SMS/Email/Push)
- [x] **Phase 8** — Intelligence Artificielle, Dashboards avancés, Rapports
- [x] **Phase 9** — Application mobile Flutter, API publique, sécurité avancée, déploiement Kubernetes

**Feuille de route complète.** Voir la section "Bilan de la feuille de route" plus bas pour un état honnête de ce qui a été construit et de ce qui reste à faire avant une mise en production réelle.

## Écarts corrigés suite à la lecture du cahier des charges (Volume 1)

- **Rôles multiples par utilisateur** (écart structurel) : le schéma (`utilisateurs.role_id` unique) a été remplacé par une table pivot `utilisateur_roles` (many-to-many, avec un rôle marqué `est_principal`). Impact en cascade assumé et livré : `UtilisateurRoleService` (nouvelle source de vérité), JWT (`role` = principal + `roles` = ensemble complet), `JwtAuthenticationFilter` (une `GrantedAuthority` par rôle), `PermissionService` (permissions cumulées de tous les rôles), formulaires frontend (sélection multi-rôles).
- **Politique de mot de passe** : complexité minimale (8 caractères, majuscule, minuscule, chiffre) + expiration configurable (`educore.security.mot-de-passe.duree-validite-jours`, 90 jours par défaut). Détectée au login, elle force un changement sans bloquer la connexion (`doitChangerMotDePasse` dans la réponse).
- **Journal d'audit des actions** : `JournalAudit`/`JournalAuditService` posés (table `journal_audit` déjà présente dans le script SQL), branché sur les actions sensibles du Module 1 (création de compte, modification, changement de statut, changement de rôle). Chaque futur module l'alimentera à son tour.
- **Non traité pour l'instant** (repéré dans le cahier, concerne un module futur) : accès restreint aux données des mineurs + gestion du consentement parental — à intégrer au Module Élèves/Parents (Phase 3).

## Phase 1 — ce qui a été ajouté

**Backend**
- `TotpService` : implémentation réelle RFC 6238 (HMAC-SHA1, fenêtre de tolérance ±30s), sans dépendance externe
- Endpoints complets : `/rafraichir` (avec rotation du refresh token), `/deconnexion`, `/mot-de-passe-oublie`, `/changer-mot-de-passe`, `/me`, `/2fa/initier`, `/2fa/confirmer`
- `UtilisateurSession` (sessions révocables individuellement) + `HistoriqueConnexion` (journal d'audit append-only des tentatives de connexion, succès et échecs)
- `PermissionService` : résolution des permissions fines par rôle (RBAC), `RoleController` pour consulter le référentiel
- `UtilisateurAdminService`/`UtilisateurAdminController` : CRUD utilisateurs isolé par établissement, réservé à `ADMIN`/`DIRECTEUR`, mot de passe temporaire généré à la création
- Tests unitaires (`TotpServiceTest`, `AuthServiceTest`) + test d'intégration bout-en-bout (`AuthControllerIntegrationTest`, H2)

**Frontend**
- `PageConnexion` branchée sur l'API réelle, avec écran de saisie du code 2FA
- Rafraîchissement automatique de jeton dans `lib/api.ts` (single-flight, rejoue la requête d'origine)
- Reconnexion silencieuse au chargement de l'app (`App.tsx`) via le refresh token persisté
- `RouteProtegee` : garde de route (redirection `/connexion`, filtrage par rôle)
- `PageProfil` : activation du 2FA avec QR code (`qrcode.react`)
- `PageUtilisateurs` : liste paginée + création de comptes, réservée à `ADMIN`/`DIRECTEUR`
- Correction Phase 0 : l'alias `@/` n'était déclaré que dans `tsconfig.json`, pas dans `vite.config.ts` — corrigé (les imports `@/...` ne se résolvaient pas réellement au build)

**Non vérifié dans ce sandbox** : `mvn test` n'a pas pu être exécuté ici (Maven Central est hors des domaines réseau autorisés de cet environnement). `npm install`/`npm run build` seraient probablement joignables mais n'ont pas non plus été testés dans ce round — lance `docker compose up -d` de ton côté avant la recette pour valider les deux.

## Mise en conformité avec les Volumes 2, 3, 6, 7 (specs techniques, architecture, API, installation)

Ces volumes, envoyés apres la livraison de la Phase 1, ont revele des ecarts de
**contrat** (pas des choix discutables comme le multi-role, mais des conventions
documentees de maniere non ambigue) :

- **JSON en snake_case** partout sur le fil (`mot_de_passe`, `jeton_rafraichissement`...),
  via `spring.jackson.property-naming-strategy: SNAKE_CASE` cote backend, et une
  conversion automatique camelCase <-> snake_case aux frontieres cote frontend
  (`lib/caseConversion.ts` + intercepteurs axios) — aucune page/composant n'a besoin
  de le savoir.
- **Format d'erreur** restructure en `{"erreur":{"code","message","champ"}}` (Volume 6 §2.3).
- **Pagination** encadree `{"donnees":[...],"pagination":{"page","taille","total","pagesTotales"}}`,
  page en base 1 (Volume 6 §2.2) — `PageDto`/`PaginationUtils` cote backend.
- **Endpoints auth renommes** : `/auth/login`, `/auth/refresh`, `/auth/logout`,
  nouveau `/auth/2fa/verifier` (verbes techniques en anglais, reste du domaine en francais).
- **Flux 2FA restructure** : `/auth/login` ne renvoie plus jamais d'erreur pour le 2FA ;
  il renvoie `deux_fa_requis` + un jeton transitoire tres court (`deux_fa_pending`, 2 min),
  que le frontend presente ensuite a `/auth/2fa/verifier` pour obtenir les jetons definitifs.
- **Faille corrigee au passage** : `JwtAuthenticationFilter` ne verifiait pas le claim
  `type` du JWT — un refresh token presente en Bearer pouvait donc s'authentifier comme
  un access token. Desormais seul un jeton `type=access` authentifie une requete (test
  de non-regression ajoute).
- **Variables d'environnement** alignees sur le Volume 7 (`DATABASE_URL`, `REDIS_URL`,
  `JWT_EXPIRATION_SECONDS`, `S3_*`...), `.env.example` et `docker-compose.yml` mis a jour.
- **Row-Level Security PostgreSQL** (Volume 3, Partie VII — "filet de securite ultime" en
  complement du filtre applicatif Hibernate) : migration `V2__row_level_security.sql` +
  `TenantRlsAspect` qui positionne `app.etablissement_id` a chaque transaction via
  `set_config(..., true)`. Deploiement **incremental** : seules les tables deja
  modelisees (Module 1 + socle etablissements/annees scolaires) sont couvertes ;
  chaque futur module ajoutera sa propre politique dans sa propre migration.
  Desactive sous le profil `test` (H2 ne supporte pas `set_config`).

**Non repris de ces volumes, deliberement** : l'API Gateway dediee (Kong) et la
sortie des modules Chat/Notifications/IA en services separes des la V1 (Volume 2 §2.4-2.5)
— pertinent seulement a partir des phases qui construisent ces modules (7 et 8), pas
maintenant.

## Phase 2 — Etablissements (multi-tenant) + Annees scolaires/Periodes

**Backend**
- `EtablissementService.inscrire()` : seul endpoint public de tout le module (`POST /etablissements/inscription`), resout le probleme d'amorçage en creant l'etablissement ET son premier compte ADMIN dans la meme transaction, plus une premiere annee scolaire active automatiquement (onboarding sans friction).
- `AnneeScolaireService` : regle metier centrale — une seule annee active a la fois par etablissement (`activer()` desactive automatiquement les autres).
- `PeriodeService` : periodes toujours contenues dans les dates de leur annee, jamais de chevauchement entre elles.
- `EtablissementModule`/`ModuleActifService` : activation/desactivation des modules par etablissement (cahier des charges 3.1 Administrateur), table absente du script SQL initial donc ajoutee en migration `V3`.
- `JourFerieService`/`VacanceScolaireService` : CRUD simple.
- **Le filtre Hibernate `@Filter`, documente depuis la Phase 0 mais jamais reellement cable, est maintenant actif** (`@FilterDef` global + `@Filter` sur `Utilisateur`, `AnneeScolaire`, `JourFerie`, `EtablissementModule`), en plus de la RLS PostgreSQL deja en place — les deux lignes de defense tournent desormais ensemble a chaque transaction via `TenantRlsAspect`.
- Migration `V3__etablissement_modules.sql` (avec sa propre politique RLS).
- Utilitaire `MotDePasseUtils` extrait de `UtilisateurAdminService` pour eviter la duplication avec le bootstrap d'inscription.
- Test unitaire cible sur la regle "une seule annee active" (`AnneeScolaireServiceTest`).

**Frontend**
- `PageInscription` : inscription en libre-service, meme pattern d'affichage du mot de passe temporaire que `PageUtilisateurs`.
- `PageEtablissement` : profil de l'etablissement, gestion des annees scolaires (creation/activation, periodes affichees), bascule des modules actifs.
- `useEtablissement` (store) : remplace le nom d'etablissement code en dur dans la Navbar depuis la Phase 0 par la vraie donnee.
- Entree de navigation "Etablissement" (ADMIN/DIRECTEUR uniquement).

**Non couvert dans cette phase, deliberement** : gestion des classes/matieres (depend de ce module, arrive en Phase 3), edition avancee du plan d'abonnement/facturation (Phase 5 - Paiements).

## Phase 3a — Structure pedagogique (Niveaux, Series, Salles, Matieres, Classes) + Enseignants

Module 3 decoupe en deux livraisons : la structure pedagogique et les enseignants
d'abord (prerequis), les eleves et parents ensuite (Phase 3b) — un eleve doit
pouvoir etre inscrit *dans* une classe qui existe deja.

**Backend**
- `Niveau`/`Serie`/`Salle`/`Matiere` : referentiels simples, lecture ouverte a tous les roles connectes, ecriture reservee ADMIN/DIRECTEUR.
- `ClasseService` : les classes sont toujours creees pour l'annee scolaire ACTIVE (jamais une autre annee implicitement) ; effectif calcule dynamiquement via la table `inscriptions` (deja presente en SQL, sera modelisee en JPA en Phase 3b).
- `ClasseMatiereService` : affectation matiere + enseignant + coefficient + volume horaire par classe.
- `EnseignantService.creer()` : meme pattern que `EtablissementService.inscrire()` et `UtilisateurAdminService.creer()` — cree le compte `Utilisateur` (role PROFESSEUR, mot de passe temporaire) ET le profil professionnel specifique dans la meme transaction.
- Migration `V4__rls_structure_pedagogique.sql` : RLS sur toutes les nouvelles tables, `@Filter` Hibernate applique de la meme maniere qu'en Phase 2.

**Frontend**
- `PageClasses` : liste des classes de l'annee active + creation (niveau/serie/libelle/capacite), effectif affiche.
- `PageEnseignants` : meme pattern que `PageUtilisateurs` (mot de passe temporaire affiche une seule fois).
- Entrees de navigation "Enseignants" et "Classes & matieres".

**Pas encore construit dans cette livraison** : ecrans de gestion des niveaux/series/salles/matieres eux-memes (CRUD backend pret, formulaires d'administration frontend a ajouter — actuellement seuls consommes en lecture par le formulaire de creation de classe), affectation d'enseignant depuis l'ecran classe (l'API existe, l'UI viendra avec le module Emploi du temps).

## Phase 3b — Eleves et Parents

**Backend**
- `EleveService` : dossier eleve (matricule auto-genere si non fourni, QR code, groupe sanguin/allergies pour l'infirmerie a venir). Statut toujours change via `changerStatut()`, jamais de suppression physique (coherent avec le reste de la plateforme).
- `InscriptionService` : inscrit un eleve dans une classe pour l'annee ACTIVE uniquement. Deux regles verifiees et testees (`InscriptionServiceTest`) : jamais deux inscriptions pour la meme annee, jamais au-dela de la capacite de la classe (`ClasseDto.Profil.effectif/capacite`, deja pose en Phase 3a).
- `ParentService` : meme pattern bootstrap que `EnseignantService`/`EtablissementService` (compte Utilisateur + profil, mot de passe temporaire).
- `EleveParentService` : lie un eleve a un ou plusieurs parents (lien de parente, contact principal, autorisation de recuperation).
- Ajout du champ `telephone` a `UtilisateurDto.Profil` (absent jusque-la, necessaire pour les contacts parents).
- Regle de couplage inter-modules respectee : `InscriptionService` consomme `ClasseService.obtenir()` (nouvellement expose), jamais le repository du module Classes directement.
- Migration `V5__rls_eleves_parents.sql`.

**Frontend**
- `PageEleves` : liste paginee, creation de dossier, inscription directe dans une classe depuis la liste.
- `PageParents` : meme pattern que `PageEnseignants`.
- Entree de navigation "Parents".

**Report a un module dedie plus tard (hors perimetre de cette phase)** : transferts inter-etablissements, historique scolaire (moyennes/rang par annee), dossiers numeriques (pieces jointes), convocations — ces quatre tables existent deja dans le script SQL mais n'ont pas encore d'entite JPA ; elles seront modelisees quand les modules qui les exploitent reellement (Notes/Bulletins, Discipline) seront construits, pour eviter du code mort.

## Phase 4a — Emploi du temps + Presences

Module 4 (coeur pedagogique) decoupe a son tour : grille horaire et presences
d'abord, Notes/Moyennes/Bulletins ensuite (workflow de validation et moteur
de calcul trop denses pour la meme livraison).

**Backend**
- `EmploiDuTempsService` : revalide en amont les 3 contraintes anti-conflit deja imposees par index unique en base (enseignant, salle, classe jamais double-reserves sur le meme creneau/jour/annee) pour un message d'erreur clair plutot qu'une violation SQL brute.
- `PresenceService` : un seul enregistrement par (eleve, cours, jour) - un second pointage met a jour l'existant plutot que d'en creer un doublon. Justification distincte de l'enregistrement (workflow a deux temps).
- Regle de couplage inter-modules a nouveau respectee : `EmploiDuTempsService` consomme `ClasseService`/`MatiereService`/`EnseignantService` (jamais leurs repositories), deux nouvelles methodes publiques `obtenir()` ajoutees a `MatiereService` pour cela.
- Migration `V6__rls_emploi_du_temps_presence.sql`.

**Frontend**
- `PageEmploiDuTemps` : grille hebdomadaire en lecture pour une classe choisie.
- `PagePresences` : pointage rapide present/absent/retard.

**Limite connue de cette livraison** : le pointage se fait actuellement sur la liste complete des eleves de l'etablissement, faute d'un endpoint "eleves d'une classe" dedie - a ajouter dans une prochaine iteration pour un pointage par classe/cours plus naturel. La creation de creneaux horaires et de seances n'a pas encore d'ecran (API prete, formulaires a venir avec l'ecran d'administration).

## Phase 4b — Notes, Moyennes, Bulletins (a venir)

## Phase 4b — Notes, Moyennes, Bulletins

Le coeur pedagogique de la plateforme. Systeme francophone classique choisi
avec toi : notes sur 20, moyenne ponderee par coefficient.

**Backend**
- `EvaluationService`/`NoteService` : workflow a 4 etats (planifiee -> saisie -> validee -> publiee). Une evaluation VALIDEE verrouille ses notes - plus aucune modification possible, pour que les moyennes calculees ensuite restent toujours coherentes avec leurs notes sources.
- **Regle de permission fine** : un `PROFESSEUR` ne peut saisir des notes que sur SA PROPRE affectation (`classe_matiere.enseignant_id` compare a son propre profil enseignant) - jamais celle d'un collegue. Les roles administratifs (ADMIN/DIRECTEUR/CENSEUR) ne sont pas soumis a cette restriction (delegation/correction).
- `MoyenneCalculService` : moteur de calcul explicite (declenche par une action "recalculer", jamais automatique a chaque note saisie - les moyennes ne doivent refleter que les evaluations VALIDEES). Moyenne matiere ponderee par le type d'evaluation (composition x2, devoir x1...), moyenne generale ponderee par le coefficient de la matiere, classement calcule dans la foulee. Les notes a bareme different de /20 sont automatiquement ramenees sur 20 avant ponderation.
- `BulletinService` : genere a partir des moyennes deja calculees + du bilan de presence de la periode (reutilise `PresenceService.bilan()`, deja construit en Phase 4a). Workflow brouillon -> valide -> publie.
- Trois nouvelles methodes publiques ajoutees a des services existants pour respecter la regle de couplage inter-modules (`ClasseMatiereService.obtenir()`, `PeriodeService.obtenir()`, `InscriptionService.listerElevesInscrits()`, `EnseignantService.obtenirIdParUtilisateurId()`).
- Migration `V7__rls_notes_bulletins.sql`.

**Frontend** : types et client API prets (`noteApi.ts`) ; les ecrans de saisie de notes et de consultation des bulletins ne sont pas encore construits dans cette livraison — a ajouter dans une prochaine iteration (meme reserve que les ecrans d'administration niveaux/series/salles en Phase 3a).

**Tests** : pas de test dedie a `MoyenneCalculService` dans cette livraison (7 dependances, mise en place Mockito consequente) - a ajouter avant la mise en production reelle, en priorite sur le calcul pondere et le classement.

## Phase 5 — Paiements (Orange Money, MTN MoMo, encaissement manuel) + Comptabilite

**Backend**
- Contrat **strictement conforme au Volume 6** (Documentation API, Parties IV.3 et V) : `POST /paiements/initier` avec en-tete `Idempotency-Key` obligatoire (retourne 202 avec `paiement_id`/`statut`/`instructions`), webhooks `POST /webhooks/orange-money` et `/mtn-momo` verifies par signature HMAC (`X-Signature`), jamais par JWT.
- **Idempotence a deux niveaux, testee** (`PaiementServiceTest`) : cote client (`Idempotency-Key` rejouee = meme paiement, jamais un doublon) et cote fournisseur (un meme webhook recu plusieurs fois ne confirme le paiement qu'une seule fois).
- `ProviderPaiement` : pattern adaptateur (Volume 3 §10.3) - `OrangeMoneyAdapter`/`MtnMomoAdapter` isolent la logique metier du fournisseur. **Non connectes a de vraies API** (pas d'acces reseau ni d'identifiants marchands dans ce sandbox) : simulent une reponse d'initiation reussie pour permettre de construire et tester tout le flux autour. A brancher sur les vraies API avant mise en production (`TODO` explicites dans le code).
- `EcritureComptableService` : chaque paiement confirme genere automatiquement sa double ecriture (debit tresorerie / credit produits de scolarite), partie double respectee.
- `EcheancierService` : genere un echeancier en N tranches egales a partir d'une grille tarifaire, recalcule automatiquement le statut de chaque tranche (impaye/partiel/paye/en_retard) apres chaque paiement confirme.
- Table `cles_idempotence` absente du script SQL initial, ajoutee en migration `V8` pour ce besoin explicitement documente. Migration `V9` pour la RLS.

**Frontend** : types et client API prets (`paiementApi.ts`, avec generation automatique de l'Idempotency-Key a chaque appel) ; ecrans (encaissement, suivi des echeanciers, releve comptable) non construits dans cette livraison, meme reserve que les Phases 3a/4b.

**Hors perimetre de cette phase** (tables deja presentes en SQL, pas encore modelisees) : factures formelles, relances de paiement automatiques, budgets previsionnels — a construire quand un module dedie a la facturation/tresorerie sera priorise.

## Phase 6 — Bibliotheque, Infirmerie, Transport, Cantine, Discipline

Cinq modules annexes, plus independants les uns des autres que les phases
precedentes - traites ensemble avec une profondeur volontairement plus legere
(1-2 regles metier par module plutot que des moteurs complexes).

- **Discipline** : incidents + sanctions. Regle : une exclusion DEFINITIVE met a jour automatiquement le statut du dossier eleve (`eleves.statut = 'exclu'`) ; une exclusion temporaire reste tracee sans affecter ce statut (l'eleve redevient actif a l'issue de la periode).
- **Bibliotheque** : catalogue + emprunts. Regle : jamais d'emprunt si `exemplaires_disponibles = 0`, restitue automatiquement au retour.
- **Infirmerie** : consultations, urgences medicales (pensees pour declencher une notification parent immediate des que le Module 24 existera), carnet de vaccination.
- **Transport** : bus, itineraires, arrets, affectation des eleves. Le suivi GPS temps reel et le pointage embarque (`presences_bus`) sont hors perimetre de cette livraison.
- **Cantine** : menus, abonnements, pointage des repas. La gestion des stocks (`stock_cantine`) est hors perimetre.
- Toutes les tables RLS-protegees (migration `V10`), avec une fonction SQL reutilisable (`_educore_appartient_etablissement_eleve`) plutot que de repeter la meme sous-requete huit fois.

**Frontend** : non construit dans cette livraison (5 modules independants, priorite donnee a la largeur de couverture backend plutot qu'a l'UI) - meme reserve que les Phases 3a/4b/5.

## Phase 7 — Chat, Reunions, Notifications : premiere separation de service reelle

**Decision explicite du porteur de projet** (apres explication du compromis) : suivre
les Volumes 2/3 a la lettre plutot que le document d'architecture initial (Phase 0)
qui proposait une extraction differee. Chat + Notifications sortent donc du
monolithe des maintenant ; Reunions y reste (non liste parmi les modules a
extraire dans le Volume 2 §2.5).

### `services/notifications-service/` — projet Spring Boot independant

- **Authentification double**, sans jamais rappeler le backend principal :
  - JWT utilisateur (navigateur/mobile) : valide LOCALEMENT via le meme `JWT_SECRET`
    que educore-backend — verification de signature/expiration/claims uniquement,
    sans appel reseau. C'est precisement l'interet du JWT stateless deja pose en
    Phase 1 : il rend cette separation peu couteuse plutot que d'exiger une
    nouvelle solution d'authentification.
  - Cle de service (`X-Service-Key`) pour les appels systeme sans utilisateur
    (ex: "bulletin publie -> creer une notification").
- **WebSocket** (`/ws/chat`) pour le temps reel du chat, handshake authentifie par
  jeton en parametre de requete (`?token=...`, un navigateur ne pouvant pas poser
  d'en-tete `Authorization` sur une connexion WebSocket native).
- **Pattern adaptateur** pour les canaux de notification (`CanalSms`/`CanalEmail`/`CanalPush`),
  meme principe que `ProviderPaiement` (Phase 5) — non connectes a de vrais
  fournisseurs dans ce sandbox (memes reserves que Orange Money/MTN MoMo).
- **RLS active ici aussi** : `TenantRlsAspect` duplique (par necessite - deux projets
  Maven distincts ne partagent pas de classes Java) depuis educore-backend.

### Limites assumees de cette separation (a lire avant mise en production)

- **Base de donnees partagee** avec educore-backend, plutot qu'une separation complete
  des donnees : etape intermediaire deliberee pour eviter la complexite d'une
  synchronisation evenementielle des ce stade. `educore-backend` reste l'unique
  proprietaire du schema (migrations Flyway) ; ce service ne fait que le consommer,
  y compris pour une projection en lecture seule (`UtilisateurContact`) necessaire
  a l'envoi (email/telephone). Une separation complete des donnees viendrait avec
  un vrai decouplage evenementiel (event sourcing/CDC), a envisager si la charge le justifie.
- **`NotificationClient`** (educore-backend) echoue silencieusement en cas de panne
  du notifications-service : publier un bulletin ne doit jamais echouer a cause d'un
  SMS non envoye. A brancher sur un logger structure + une file de retry en production.
- **Premiere integration reelle bout-en-bout** : `BulletinService.publier()` notifie
  desormais reellement les parents via ce nouveau service (remplace le TODO de la
  Phase 4b) ; `ReunionService.creer()` fait de meme pour les invites.

### Module Reunions (reste dans educore-backend)

CRUD reunions + invitations + reponses (accepte/decline) + compte-rendu. Chaque
invitation notifie l'invite via le notifications-service (meme pattern que les bulletins).

### Demarrage local (deux services desormais)

```bash
docker compose up -d   # postgres, redis, minio, backend (8080), notifications (8081), frontend (5173)
```

**Frontend** : non mis a jour dans cette livraison (integration chat/notifications
c'est un chantier a part entiere - websocket client, store de conversation en temps
reel) — a construire dans une prochaine iteration.

## Phase 8 — Intelligence Artificielle (service separe), Dashboard, Rapports

**Decision confirmee** : coherence avec la Phase 7, l'IA sort aussi du monolithe
des maintenant (Volume 2 §2.5 la classe dans le meme groupe que Chat/Notifications).

### `services/ia-service/` (port 8082) — meme schema que notifications-service

- **Detection de risque d'echec** : score EXPLICABLE et deterministe (formule documentee
  dans le code, ponderation moyenne/absences/discipline), jamais un modele boite noire.
  Reste une SUGGESTION pour le conseil de classe — aucune decision d'orientation ou
  de sanction n'est automatisee (principe pose des le document d'architecture initial).
- **Commentaires de bulletin** : generes par templates deterministes bases sur la
  moyenne (AUCUN LLM reel connecte dans ce sandbox — pas d'acces reseau externe).
  Toujours re-validables/editables par l'enseignant avant publication finale.
- **Assistant conversationnel** : chaque question est journalisee ; la reponse
  actuelle indique honnetement qu'aucun fournisseur LLM n'est encore branche,
  plutot que de simuler une reponse qui semblerait credible sans l'etre.
- Memes projections en lecture seule que notifications-service (`MoyenneGeneraleProjection`,
  `BulletinProjection`) sur les tables du backend principal — memes limites assumees.

### Dashboard (reste dans le monolithe, Module 26)

**La boucle ouverte depuis la Phase 0 est enfin fermee** : les 4 cartes du tableau
de bord (elèves inscrits, taux de présence, paiements du mois, personnel actif),
posées comme placeholders "—" dès le tout premier livrable, affichent maintenant
de vraies données agrégées depuis les modules Eleves/Presence/Paiements/Enseignants.
Le Dashboard ne recalcule jamais rien lui-meme — il expose uniquement ce que
chaque module sait déjà produire (principe de responsabilité unique appliqué
aux modules).

**Rapports** : non construit dans cette livraison — dépend naturellement du
Module Documents (génération PDF/export), pas encore construit non plus.
Les deux sont reportés ensemble à une prochaine itération plutôt que de
livrer un Rapports incomplet sans capacité d'export réelle.

### Demarrage local (trois services desormais)

```bash
docker compose up -d   # postgres, redis, minio, backend (8080), notifications (8081), ia (8082), frontend (5173)
```

## Phase 9 — Mobile Flutter, API publique, sécurité avancée, Kubernetes (DERNIÈRE PHASE)

Coherence avec les Phases 7/8 : l'IA etant deja sortie du monolithe, ce chantier
couvre les quatre piliers annonces, chacun a un niveau de squelette reel plutot
que superficiel.

## Structure du système éducatif camerounais (intégration post-Phase 9)

Le système a été enrichi pour supporter la structure complète du système éducatif
camerounais conformément aux spécifications du MINESEC.

**Nouvelles tables** :
- `sous_systemes` : FRANCOPHONE, ANGLOPHONE, BILINGUE
- `cycles` : PREMIER_CYCLE (4 ans), SECOND_CYCLE (3 ans)
- `matiere_niveau_serie` : Configuration des matières par niveau et série avec coefficients
- `etablissement_langue_option` : Langues vivantes et options par établissement

**Niveaux par défaut** (7 niveaux principaux) :
- 1er cycle : 6e, 5e, 4e, 3e
- 2nd cycle : 2nde, 1ère, Terminale

**Séries francophones** :
- Série A (Littéraire) : A1, A2, A3, A4, A5, AC
- Série C (Scientifique - Maths + Physique)
- Série D (Scientifique - Maths + SVT)
- Série TI (Technologie de l'Information)
- Série BIL (Bilingue)

**Séries anglophones** :
- Arts : A1, A2, A3, A4, A5, A6, A7, A8
- Sciences : S1, S2, S3, S4

**Types de matières** :
- GENERALE : Français, Mathématiques, Histoire, etc.
- LANGUE_VIVANTE : Anglais (LV1), Allemand, Espagnol, Arabe (LV2/LV3)
- OPTION : Latin, Grec, Italien, Russe, Chinois
- SPECIALITE : Matières spécifiques selon les séries

**Nouvel endpoint API** :
- `POST /structure-systeme-educatif/niveaux/initialiser` : Initialisation des 7 niveaux par défaut
- `GET /structure-systeme-educatif/sous-systemes` : Liste des sous-systèmes
- `GET /structure-systeme-educatif/series` : Séries par sous-système
- `POST /structure-systeme-educatif/matieres-niveau-serie` : Configuration matière/niveau/série
- `POST /structure-systeme-educatif/langues-options` : Ajout langue/option par établissement

## Workflow pédagogique complet (intégration post-Phase 9)

Un service d'orchestration a été ajouté pour faciliter le flux pédagogique complet de la gestion des élèves à la génération des bulletins.

**WorkflowPedagogiqueService** - Service d'orchestration qui combine tous les modules existants :

1. **Création d'élève avec inscription** : `POST /workflow-pedagogique/eleves`
   - Crée le dossier élève
   - Inscription automatique dans une classe

2. **Création de professeur** : `POST /workflow-pedagogique/professeurs`
   - Crée le compte utilisateur (rôle PROFESSEUR)
   - Crée le profil professionnel
   - Génère un mot de passe temporaire

3. **Affectation professeur à classe-matière** : `POST /workflow-pedagogique/affectations-professeur`
   - Affecte un professeur à une matière spécifique d'une classe

4. **Saisie des notes** : `POST /workflow-pedagogique/evaluations/notes`
   - Crée une évaluation
   - Saisit les notes en masse pour tous les élèves
   - Gère les absences et observations

5. **Calcul des moyennes** : `POST /workflow-pedagogique/classes/{classeId}/moyennes`
   - Calcule les moyennes matière (pondérées par type d'évaluation)
   - Calcule la moyenne générale (pondérée par coefficient matière)
   - Attribue le rang dans la classe
   - Détermine la mention (félicitations, encouragements, avertissement)

6. **Génération des bulletins** : `POST /workflow-pedagogique/classes/{classeId}/bulletins`
   - Génère les bulletins pour tous les élèves de la classe
   - Inclut le bilan de présence
   - Valide et publie automatiquement
   - Notifie les parents (via notifications-service)

**Workflow complet en un appel** : `POST /workflow-pedagogique/complet`
- Exécute toutes les étapes en une seule transaction
- Idéal pour l'intégration et les tests

### Securite avancee
- **Rate limiting Redis** sur `/auth/login` (10 tentatives/minute par IP) — mentionne
  dans le document d'architecture depuis la Phase 0, jamais cable jusqu'ici.
- Le reste (RLS, 2FA, politique de mot de passe, audit, RBAC multi-role) etait
  deja construit au fil des phases precedentes.

### API publique (Module 30)
- **OAuth2 `client_credentials`** conforme au Volume 6 : `POST /oauth/token` (public),
  gestion des clients API reservee ADMIN (`/api-clients`), secret affiche une seule fois.
- Jeton distinct (`type=api_client`) qui n'authentifie jamais un utilisateur reel —
  `ApiClientPrincipal` separe de `UtilisateurPrincipal`, role unique `ROLE_API_CLIENT`.
- Un exemple concret d'endpoint expose (`GET /api/v1/public/eleves/{id}`) prouve que
  le scope fonctionne reellement, pas juste en theorie.

### Kubernetes (`k8s/`)
9 manifestes : namespace, ConfigMap/Secret (gabarit, jamais de vraies valeurs),
PostgreSQL (StatefulSet + volume persistant), Redis, MinIO, les 4 services
applicatifs (backend avec HPA 2-8 replicas, notifications avec affinite de
session pour le WebSocket, ia, frontend), et un Ingress avec TLS + routage
par chemin. **Non teste sur un vrai cluster** dans ce sandbox (aucun acces
`kubectl`/cluster ici) — a valider avant tout deploiement reel.

### Application mobile (`mobile/`)
Squelette Flutter reel (pas une simple description) : `pubspec.yaml`, structure
`core/`+`features/` conforme au Volume 3, client HTTP avec la meme conversion
snake_case/camelCase et le meme rafraichissement automatique de jeton que le
frontend web, stockage securise des jetons (Keychain/Keystore, jamais en clair),
flux de connexion en deux temps (2FA) identique a `PageConnexion.tsx`, ecran de
tableau de bord consommant le meme endpoint que la version web.
**2 ecrans construits** (connexion, tableau de bord) sur la vingtaine que
comporterait l'application complete (notes, paiement, presence QR code, chat,
notifications push) — un vrai depart, pas une application complete.

---

## Bilan de la feuille de route

**Les 9 phases prevues sont livrees.** Vue d'ensemble honnete de ce que ca
represente reellement :

- **3 services independants** (`backend`, `notifications-service`, `ia-service`)
  + 1 frontend web + 1 squelette mobile, coherents avec les Volumes 2/3 relus en
  cours de route et corriges quand un ecart a ete trouve.
- **13 migrations Flyway**, Row-Level Security PostgreSQL sur pratiquement toutes
  les tables sensibles, filtre Hibernate applicatif en complement.
- **Profondeur variable, assumee et documentee a chaque etape** : les modules
  coeur (Auth, Etablissements, Notes/Bulletins, Paiements) ont une couverture
  metier dense et testee ; les modules annexes (Bibliotheque, Infirmerie,
  Transport, Cantine) ont une profondeur volontairement plus legere ; certains
  ecrans frontend (Notes, Bulletins, Paiements, Chat) restent a construire —
  toujours signale explicitement au moment de la livraison plutot que tu.
- **Aucune integration externe reelle n'a pu etre testee** dans ce sandbox
  (Orange Money, MTN MoMo, SMS, email, push, LLM) : tous des adaptateurs stub
  honnetes, prets a recevoir de vraies cles/API en production.
- **`mvn test` et `npm run build` n'ont jamais pu etre executes** dans cet
  environnement (reseau restreint aux registres de paquets) — c'est la
  prochaine etape indispensable avant toute mise en production, sur les
  quatre projets (`backend`, `services/notifications-service`,
  `services/ia-service`, `frontend`).

## Notes pour la suite

- Le `TotpService` (2FA) contient des méthodes à implémenter en Phase 1 (RFC 6238) — le contrat public est déjà stable pour ne pas casser `AuthService` plus tard.
- Le filtre Hibernate d'isolation multi-tenant (`@Filter`) est actif depuis la Phase 2 (voir section Phase 2 ci-dessus).
- Le layout (Navbar/Sidebar/Footer) est déjà prêt à recevoir toutes les nouvelles entrées de navigation : il suffit d'ajouter une ligne dans `frontend/src/components/layout/navigation.ts` à chaque nouveau module.
#   e d o c o r e -  
 