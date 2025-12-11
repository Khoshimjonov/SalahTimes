@echo off
setlocal enabledelayedexpansion

echo ============================================
echo  SalahTimesWidget - EXE Builder
echo ============================================
echo.

:: ===========================================
:: Configuration
:: ===========================================
set APP_NAME=SalahTimesWidget
set APP_VERSION=2.0.0
set MAIN_CLASS=uz.khoshimjonov.Main
set VENDOR=Khoshimjonov
set DESCRIPTION=Prayer Times Desktop Widget

:: Directories
set BUILD_DIR=build
set TEMP_DIR=%BUILD_DIR%\temp
set RELEASE_DIR=%BUILD_DIR%\release

:: ===========================================
:: Pre-checks
:: ===========================================
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set
    exit /b 1
)

echo Java: %JAVA_HOME%
echo.

:: ===========================================
:: Step 1: Clean
:: ===========================================
echo [1/6] Cleaning previous builds...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%TEMP_DIR%\input"
mkdir "%RELEASE_DIR%\production"
mkdir "%RELEASE_DIR%\debug"
echo Done.
echo.

:: ===========================================
:: Step 2: Maven build
:: ===========================================
echo [2/6] Building with Maven...
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo ERROR: Maven build failed
    exit /b 1
)

:: Copy JAR to temp input
copy "target\%APP_NAME%.jar" "%TEMP_DIR%\input\" >nul
if errorlevel 1 (
    echo ERROR: JAR not found. Check maven-shade-plugin config.
    exit /b 1
)
echo Done.
echo.

:: ===========================================
:: Step 3: Create custom JRE
:: ===========================================
echo [3/6] Creating custom JRE...
"%JAVA_HOME%\bin\jlink" ^
    --add-modules java.base,java.desktop,java.logging,java.prefs,java.net.http,jdk.crypto.ec,jdk.localedata ^
    --include-locales=en,ru,uz ^
    --strip-debug ^
    --no-man-pages ^
    --no-header-files ^
    --compress=zip-6 ^
    --output "%TEMP_DIR%\runtime"

if errorlevel 1 (
    echo ERROR: jlink failed
    exit /b 1
)
echo Done.
echo.

:: ===========================================
:: Step 4: Check icon
:: ===========================================
set ICON_OPTION=
if exist "src\main\resources\images\main.ico" (
    set ICON_OPTION=--icon "src\main\resources\images\main.ico"
    echo Using custom icon.
) else (
    echo No icon found, using default.
)
echo.

:: ===========================================
:: Step 5: Create production build
:: ===========================================
echo [4/6] Creating production build...
"%JAVA_HOME%\bin\jpackage" ^
    --verbose ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --input "%TEMP_DIR%\input" ^
    --main-jar "%APP_NAME%.jar" ^
    --main-class "%MAIN_CLASS%" ^
    --runtime-image "%TEMP_DIR%\runtime" ^
    --dest "%RELEASE_DIR%\production" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --description "%DESCRIPTION%" ^
    %ICON_OPTION% ^
    --java-options "-Xmx256m" ^
    --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
    echo ERROR: jpackage production build failed
    exit /b 1
)
echo Done.
echo.

:: ===========================================
:: Step 6: Create debug build
:: ===========================================
echo [5/6] Creating debug build...
"%JAVA_HOME%\bin\jpackage" ^
    --verbose ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --input "%TEMP_DIR%\input" ^
    --main-jar "%APP_NAME%.jar" ^
    --main-class "%MAIN_CLASS%" ^
    --runtime-image "%TEMP_DIR%\runtime" ^
    --dest "%RELEASE_DIR%\debug" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --description "%DESCRIPTION% (Debug)" ^
    %ICON_OPTION% ^
    --win-console ^
    --java-options "-Xmx256m" ^
    --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
    echo ERROR: jpackage debug build failed
    exit /b 1
)
echo Done.
echo.

:: ===========================================
:: Step 7: Create ZIP for distribution
:: ===========================================
echo [6/6] Creating distribution ZIP...
pushd "%RELEASE_DIR%\production"
powershell -Command "Compress-Archive -Path '%APP_NAME%' -DestinationPath '..\%APP_NAME%-%APP_VERSION%-windows.zip' -Force"
popd
echo Done.
echo.

:: ===========================================
:: Cleanup temp files (optional)
:: ===========================================
echo Cleaning temp files...
rmdir /s /q "%TEMP_DIR%"
echo Done.
echo.

:: ===========================================
:: Summary
:: ===========================================
echo ============================================
echo  BUILD SUCCESSFUL!
echo ============================================
echo.
echo  Output location: %RELEASE_DIR%\
echo.
echo  Files:
echo    production\%APP_NAME%\%APP_NAME%.exe  (for users)
echo    debug\%APP_NAME%\%APP_NAME%.exe       (for testing)
echo    %APP_NAME%-%APP_VERSION%-windows.zip  (for distribution)
echo.
echo  Sizes:

for /f "tokens=3" %%a in ('dir /s /-c "%RELEASE_DIR%\production\%APP_NAME%" 2^>nul ^| findstr "File(s)"') do (
    set /a SIZE_MB=%%a/1024/1024
    echo    Production: ~!SIZE_MB! MB
)

for %%A in ("%RELEASE_DIR%\%APP_NAME%-%APP_VERSION%-windows.zip") do (
    set /a ZIP_MB=%%~zA/1024/1024
    echo    ZIP: ~!ZIP_MB! MB
)

echo.
echo ============================================
pause