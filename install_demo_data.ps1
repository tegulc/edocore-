# Script PowerShell pour l'installation des données de démonstration EDUCORE
# Ce script exécute les scripts SQL de démonstration sur la base de données PostgreSQL

# Configuration
$projectRoot = "C:\Users\prosper computer\Desktop\educore"
$demoDataScript = "$projectRoot\demo_data.sql"
$demoNotesScript = "$projectRoot\demo_notes_evaluations.sql"

# Configuration PostgreSQL (à adapter selon votre installation)
$pgHost = "localhost"
$pgPort = "5432"
$pgDatabase = "educore"
$pgUser = "educore"
$pgPassword = "educore"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "INSTALLATION DES DONNÉES DE DÉMONSTRATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si PostgreSQL est installé
Write-Host "Vérification de PostgreSQL..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psqlPath) {
    Write-Host "PostgreSQL n'est pas trouvé dans le PATH." -ForegroundColor Red
    Write-Host "Essayons de trouver psql.exe dans les chemins d'installation courants..." -ForegroundColor Yellow
    
    $possiblePaths = @(
        "C:\Program Files\PostgreSQL\*\bin\psql.exe",
        "C:\PostgreSQL\*\bin\psql.exe",
        "${env:ProgramFiles}\PostgreSQL\*\bin\psql.exe"
    )
    
    foreach ($path in $possiblePaths) {
        $found = Get-ChildItem $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $psqlPath = $found.FullName
            Write-Host "psql trouvé: $psqlPath" -ForegroundColor Green
            break
        }
    }
    
    if (-not $psqlPath) {
        Write-Host "PostgreSQL introuvable. Veuillez:" -ForegroundColor Red
        Write-Host "1. Installer PostgreSQL" -ForegroundColor White
        Write-Host "2. Ou utiliser les scripts SQL manuellement via pgAdmin" -ForegroundColor White
        Write-Host ""
        Write-Host "Instructions manuelles:" -ForegroundColor Cyan
        Write-Host "1. Ouvrez pgAdmin" -ForegroundColor White
        Write-Host "2. Connectez-vous à la base de données 'educore'" -ForegroundColor White
        Write-Host "3. Exécutez le script: $demoDataScript" -ForegroundColor White
        Write-Host "4. Exécutez le script: $demoNotesScript" -ForegroundColor White
        exit 1
    }
}

# Vérifier si les scripts SQL existent
Write-Host "Vérification des scripts SQL..." -ForegroundColor Yellow
if (-not (Test-Path $demoDataScript)) {
    Write-Host "Script non trouvé: $demoDataScript" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $demoNotesScript)) {
    Write-Host "Script non trouvé: $demoNotesScript" -ForegroundColor Red
    exit 1
}

Write-Host "Scripts SQL trouvés avec succès." -ForegroundColor Green

# Définir la variable d'environnement pour le mot de passe
$env:PGPASSWORD = $pgPassword

# Exécuter le premier script (données de base)
Write-Host ""
Write-Host "Exécution de demo_data.sql..." -ForegroundColor Yellow
try {
    & $psqlPath -h $pgHost -p $pgPort -U $pgUser -d $pgDatabase -f $demoDataScript
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ demo_data.sql exécuté avec succès" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreur lors de l'exécution de demo_data.sql" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
    exit 1
}

# Exécuter le deuxième script (notes et évaluations)
Write-Host ""
Write-Host "Exécution de demo_notes_evaluations.sql..." -ForegroundColor Yellow
try {
    & $psqlPath -h $pgHost -p $pgPort -U $pgUser -d $pgDatabase -f $demoNotesScript
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ demo_notes_evaluations.sql exécuté avec succès" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreur lors de l'exécution de demo_notes_evaluations.sql" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
    exit 1
}

# Nettoyer la variable d'environnement
Remove-Item Env:PGPASSWORD

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "INSTALLATION TERMINÉE AVEC SUCCÈS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Données créées:" -ForegroundColor Cyan
Write-Host "- 10 professeurs (mot de passe: admin12345)" -ForegroundColor White
Write-Host "- 2 personnels administratifs (mot de passe: admin12345)" -ForegroundColor White
Write-Host "- 30 élèves répartis en 3 classes (mot de passe: admin12345)" -ForegroundColor White
Write-Host "- Affectations professeurs-matières" -ForegroundColor White
Write-Host "- Évaluations et notes pour chaque matière" -ForegroundColor White
Write-Host ""
Write-Host "Prochaines étapes:" -ForegroundColor Cyan
Write-Host "1. Démarrez l'application Spring Boot" -ForegroundColor White
Write-Host "2. Connectez-vous avec un compte admin existant" -ForegroundColor White
Write-Host "3. Utilisez l'API pour calculer les moyennes" -ForegroundColor White
Write-Host "4. Générez les bulletins via l'interface" -ForegroundColor White
Write-Host "5. Vérifiez le tableau d'honneur dans le frontend" -ForegroundColor White
Write-Host ""
Write-Host "Pour plus de détails, consultez: DEMO_README.md" -ForegroundColor Cyan