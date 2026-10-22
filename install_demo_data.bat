@echo off
REM Script Batch pour l'installation des données de démonstration EDUCORE
REM Ce script exécute les scripts SQL de démonstration sur la base de données PostgreSQL

echo ========================================
echo INSTALLATION DES DONNÉES DE DÉMONSTRATION
echo ========================================
echo.

REM Configuration
set PROJECT_ROOT=C:\Users\prosper computer\Desktop\educore
set DEMO_DATA_SCRIPT=%PROJECT_ROOT%\demo_data.sql
set DEMO_NOTES_SCRIPT=%PROJECT_ROOT%\demo_notes_evaluations.sql

REM Configuration PostgreSQL (à adapter selon votre installation)
set PG_HOST=localhost
set PG_PORT=5432
set PG_DATABASE=educore
set PG_USER=educore
set PGPASSWORD=educore

echo Vérification de PostgreSQL...
where psql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo PostgreSQL non trouvé dans le PATH.
    echo Recherche dans les chemins d'installation courants...
    
    if exist "C:\Program Files\PostgreSQL\*\bin\psql.exe" (
        for /d %%i in ("C:\Program Files\PostgreSQL\*") do (
            if exist "%%i\bin\psql.exe" (
                set PSQL_PATH=%%i\bin\psql.exe
                echo psql trouvé: !PSQL_PATH!
                goto :found_psql
            )
        )
    )
    
    if exist "C:\PostgreSQL\*\bin\psql.exe" (
        for /d %%i in ("C:\PostgreSQL\*") do (
            if exist "%%i\bin\psql.exe" (
                set PSQL_PATH=%%i\bin\psql.exe
                echo psql trouvé: !PSQL_PATH!
                goto :found_psql
            )
        )
    )
    
    echo PostgreSQL introuvable. Veuillez:
    echo 1. Installer PostgreSQL
    echo 2. Ou utiliser les scripts SQL manuellement via pgAdmin
    echo.
    echo Instructions manuelles:
    echo 1. Ouvrez pgAdmin
    echo 2. Connectez-vous à la base de données 'educore'
    echo 3. Exécutez le script: %DEMO_DATA_SCRIPT%
    echo 4. Exécutez le script: %DEMO_NOTES_SCRIPT%
    pause
    exit /b 1
)

:found_psql
set PSQL_PATH=psql

echo Vérification des scripts SQL...
if not exist "%DEMO_DATA_SCRIPT%" (
    echo Script non trouvé: %DEMO_DATA_SCRIPT%
    pause
    exit /b 1
)

if not exist "%DEMO_NOTES_SCRIPT%" (
    echo Script non trouvé: %DEMO_NOTES_SCRIPT%
    pause
    exit /b 1
)

echo Scripts SQL trouvés avec succès.
echo.

echo Exécution de demo_data.sql...
"%PSQL_PATH%" -h %PG_HOST% -p %PG_PORT% -U %PG_USER% -d %PG_DATABASE% -f "%DEMO_DATA_SCRIPT%"
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de l'exécution de demo_data.sql
    pause
    exit /b 1
)
echo ✓ demo_data.sql exécuté avec succès
echo.

echo Exécution de demo_notes_evaluations.sql...
"%PSQL_PATH%" -h %PG_HOST% -p %PG_PORT% -U %PG_USER% -d %PG_DATABASE% -f "%DEMO_NOTES_SCRIPT%"
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de l'exécution de demo_notes_evaluations.sql
    pause
    exit /b 1
)
echo ✓ demo_notes_evaluations.sql exécuté avec succès
echo.

echo ========================================
echo INSTALLATION TERMINÉE AVEC SUCCÈS
echo ========================================
echo.
echo Données créées:
echo - 10 professeurs (mot de passe: admin12345)
echo - 2 personnels administratifs (mot de passe: admin12345)
echo - 30 élèves répartis en 3 classes (mot de passe: admin12345)
echo - Affectations professeurs-matières
echo - Évaluations et notes pour chaque matière
echo.
echo Prochaines étapes:
echo 1. Démarrez l'application Spring Boot
echo 2. Connectez-vous avec un compte admin existant
echo 3. Utilisez l'API pour calculer les moyennes
echo 4. Générez les bulletins via l'interface
echo 5. Vérifiez le tableau d'honneur dans le frontend
echo.
echo Pour plus de détails, consultez: DEMO_README.md
pause