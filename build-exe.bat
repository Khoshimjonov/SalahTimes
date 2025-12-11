@echo off
setlocal enabledelayedexpansion

echo.
echo ============================================
echo  SalahTimesWidget - Production Builder
echo ============================================
echo.

:: ===========================================
:: Configuration (edit these if needed)
:: ===========================================
set APP_NAME=SalahTimesWidget
set APP_VERSION=2.0.0
set MAIN_CLASS=uz.khoshimjonov.Main
set VENDOR=Khoshimjonov
set DESCRIPTION=Prayer Times Desktop Widget
set LOCALES=en,ru,uz

:: Directories
set BUILD_DIR=build
set TEMP_DIR=%BUILD_DIR%\temp
set RELEASE_DIR=%BUILD_DIR%\release

:: ===========================================
:: Pre-flight checks
:: ===========================================
echo [Prerequisites]
echo.

:: Check JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo [ERROR] JAVA_HOME is not set.
    echo         Please set JAVA_HOME to your JDK 21 installation.
    goto :error
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java not found at JAVA_HOME: %JAVA_HOME%
    goto :error
)

:: Display Java info
echo   JAVA_HOME: %JAVA_HOME%
echo   Java Version:
"%JAVA_HOME%\bin\java.exe" -version 2>&1 | findstr /i "version"
echo.

:: Check Maven
where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven not found in PATH.
    echo         Please install Maven and add it to PATH.
    goto :error
)
echo   Maven: Found
echo.

:: Check icon file
set ICON_PATH=src\main\resources\images\main.ico
set ICON_OPTION=
if exist "%ICON_PATH%" (
    echo   Icon: Found [%ICON_PATH%]
    set "ICON_OPTION=--icon %ICON_PATH%"
) else (
    echo   Icon: Not found [using default]
)
echo.

:: ===========================================
:: Step 1: Clean previous builds
:: ===========================================
echo [1/7] Cleaning previous builds...

if exist "%BUILD_DIR%" (
    rmdir /s /q "%BUILD_DIR%" 2>nul
    if exist "%BUILD_DIR%" (
        echo [ERROR] Cannot delete %BUILD_DIR% folder.
        echo         Close any programs using files from this folder.
        goto :error
    )
)

mkdir "%TEMP_DIR%\input"
mkdir "%RELEASE_DIR%\windows"
mkdir "%RELEASE_DIR%\windows-debug"
mkdir "%RELEASE_DIR%\cross-platform"

echo   Done.
echo.

:: ===========================================
:: Step 2: Build with Maven
:: ===========================================
echo [2/7] Building with Maven...

call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo [ERROR] Maven build failed.
    echo         Run 'mvn clean package' manually to see errors.
    goto :error
)

:: Verify JAR was created
if not exist "target\%APP_NAME%.jar" (
    echo [ERROR] JAR file not found: target\%APP_NAME%.jar
    echo         Check maven-shade-plugin configuration in pom.xml
    echo.
    echo         Available JARs in target:
    dir /b target\*.jar 2>nul
    goto :error
)

:: Copy JAR to temp
copy "target\%APP_NAME%.jar" "%TEMP_DIR%\input\" >nul

echo   Done.
echo.

:: ===========================================
:: Step 3: Create cross-platform package
:: ===========================================
echo [3/7] Creating cross-platform package...

:: Copy JAR
copy "target\%APP_NAME%.jar" "%RELEASE_DIR%\cross-platform\" >nul

:: Windows launcher
echo @echo off> "%RELEASE_DIR%\cross-platform\run-windows.bat"
echo title %APP_NAME%>> "%RELEASE_DIR%\cross-platform\run-windows.bat"
echo java -jar "%APP_NAME%.jar" %%*>> "%RELEASE_DIR%\cross-platform\run-windows.bat"
echo if errorlevel 1 pause>> "%RELEASE_DIR%\cross-platform\run-windows.bat"

:: Linux/Mac launcher
echo #!/bin/bash> "%RELEASE_DIR%\cross-platform\run-linux-mac.sh"
echo cd "$(dirname "$0")">> "%RELEASE_DIR%\cross-platform\run-linux-mac.sh"
echo java -jar %APP_NAME%.jar "$@">> "%RELEASE_DIR%\cross-platform\run-linux-mac.sh"

:: README
echo ================================================================> "%RELEASE_DIR%\cross-platform\README.txt"
echo  %APP_NAME% v%APP_VERSION%>> "%RELEASE_DIR%\cross-platform\README.txt"
echo  %DESCRIPTION%>> "%RELEASE_DIR%\cross-platform\README.txt"
echo ================================================================>> "%RELEASE_DIR%\cross-platform\README.txt"
echo.>> "%RELEASE_DIR%\cross-platform\README.txt"
echo REQUIREMENTS: Java 21 or later>> "%RELEASE_DIR%\cross-platform\README.txt"
echo.>> "%RELEASE_DIR%\cross-platform\README.txt"
echo WINDOWS: Double-click run-windows.bat>> "%RELEASE_DIR%\cross-platform\README.txt"
echo LINUX/MAC: chmod +x run-linux-mac.sh ^&^& ./run-linux-mac.sh>> "%RELEASE_DIR%\cross-platform\README.txt"
echo ================================================================>> "%RELEASE_DIR%\cross-platform\README.txt"

echo   Done.
echo.

:: ===========================================
:: Step 4: Create custom JRE with jlink
:: ===========================================
echo [4/7] Creating custom JRE with jlink...

call "%JAVA_HOME%\bin\jlink.exe" ^
    --add-modules java.base,java.desktop,java.logging,java.prefs,java.net.http,jdk.crypto.ec,jdk.localedata ^
    --include-locales=%LOCALES% ^
    --strip-debug ^
    --no-man-pages ^
    --no-header-files ^
    --compress=zip-6 ^
    --output "%TEMP_DIR%\runtime"

if errorlevel 1 (
    echo [ERROR] jlink failed.
    goto :error
)

echo   Done.
echo.

:: ===========================================
:: Step 5: Create Windows production build
:: ===========================================
echo [5/7] Creating Windows production build...

if defined ICON_OPTION (
    call "%JAVA_HOME%\bin\jpackage.exe" ^
        --type app-image ^
        --name "%APP_NAME%" ^
        --input "%TEMP_DIR%\input" ^
        --main-jar "%APP_NAME%.jar" ^
        --main-class "%MAIN_CLASS%" ^
        --runtime-image "%TEMP_DIR%\runtime" ^
        --dest "%RELEASE_DIR%\windows" ^
        --app-version "%APP_VERSION%" ^
        --vendor "%VENDOR%" ^
        --description "%DESCRIPTION%" ^
        --icon "%ICON_PATH%" ^
        --java-options "-Xmx256m" ^
        --java-options "-Dfile.encoding=UTF-8" ^
        --java-options "-Dapp.version=%APP_VERSION%"
) else (
    call "%JAVA_HOME%\bin\jpackage.exe" ^
        --type app-image ^
        --name "%APP_NAME%" ^
        --input "%TEMP_DIR%\input" ^
        --main-jar "%APP_NAME%.jar" ^
        --main-class "%MAIN_CLASS%" ^
        --runtime-image "%TEMP_DIR%\runtime" ^
        --dest "%RELEASE_DIR%\windows" ^
        --app-version "%APP_VERSION%" ^
        --vendor "%VENDOR%" ^
        --description "%DESCRIPTION%" ^
        --java-options "-Xmx256m" ^
        --java-options "-Dfile.encoding=UTF-8" ^
        --java-options "-Dapp.version=%APP_VERSION%"
)

if errorlevel 1 (
    echo [ERROR] jpackage production build failed.
    goto :error
)

echo   Done.
echo.

:: ===========================================
:: Step 6: Create Windows debug build
:: ===========================================
echo [6/7] Creating Windows debug build...

if defined ICON_OPTION (
    call "%JAVA_HOME%\bin\jpackage.exe" ^
        --type app-image ^
        --name "%APP_NAME%-Debug" ^
        --input "%TEMP_DIR%\input" ^
        --main-jar "%APP_NAME%.jar" ^
        --main-class "%MAIN_CLASS%" ^
        --runtime-image "%TEMP_DIR%\runtime" ^
        --dest "%RELEASE_DIR%\windows-debug" ^
        --app-version "%APP_VERSION%" ^
        --vendor "%VENDOR%" ^
        --description "%DESCRIPTION% (Debug)" ^
        --icon "%ICON_PATH%" ^
        --win-console ^
        --java-options "-Xmx256m" ^
        --java-options "-Dfile.encoding=UTF-8" ^
        --java-options "-Dapp.version=%APP_VERSION%" ^
        --java-options "-Dapp.debug=true"
) else (
    call "%JAVA_HOME%\bin\jpackage.exe" ^
        --type app-image ^
        --name "%APP_NAME%-Debug" ^
        --input "%TEMP_DIR%\input" ^
        --main-jar "%APP_NAME%.jar" ^
        --main-class "%MAIN_CLASS%" ^
        --runtime-image "%TEMP_DIR%\runtime" ^
        --dest "%RELEASE_DIR%\windows-debug" ^
        --app-version "%APP_VERSION%" ^
        --vendor "%VENDOR%" ^
        --description "%DESCRIPTION% (Debug)" ^
        --win-console ^
        --java-options "-Xmx256m" ^
        --java-options "-Dfile.encoding=UTF-8" ^
        --java-options "-Dapp.version=%APP_VERSION%" ^
        --java-options "-Dapp.debug=true"
)

if errorlevel 1 (
    echo [ERROR] jpackage debug build failed.
    goto :error
)

echo   Done.
echo.

:: ===========================================
:: Step 7: Create distribution ZIPs
:: ===========================================
echo [7/7] Creating distribution ZIPs...

:: Windows production ZIP
pushd "%RELEASE_DIR%\windows"
powershell -Command "Compress-Archive -Path '%APP_NAME%' -DestinationPath '..\%APP_NAME%-%APP_VERSION%-windows.zip' -Force"
popd

:: Windows debug ZIP
pushd "%RELEASE_DIR%\windows-debug"
powershell -Command "Compress-Archive -Path '%APP_NAME%-Debug' -DestinationPath '..\%APP_NAME%-%APP_VERSION%-windows-debug.zip' -Force"
popd

:: Cross-platform ZIP
pushd "%RELEASE_DIR%\cross-platform"
powershell -Command "Compress-Archive -Path '*' -DestinationPath '..\%APP_NAME%-%APP_VERSION%-cross-platform.zip' -Force"
popd

echo   Done.
echo.

:: ===========================================
:: Cleanup
:: ===========================================
echo Cleaning temporary files...
rmdir /s /q "%TEMP_DIR%" 2>nul
echo   Done.
echo.

:: ===========================================
:: Summary
:: ===========================================
echo ============================================
echo  BUILD SUCCESSFUL!
echo ============================================
echo.
echo  Output: %RELEASE_DIR%\
echo.
echo  WINDOWS [Java bundled]:
echo    - %RELEASE_DIR%\windows\%APP_NAME%\%APP_NAME%.exe
echo    - %RELEASE_DIR%\windows-debug\%APP_NAME%-Debug\%APP_NAME%-Debug.exe
echo.
echo  CROSS-PLATFORM [requires Java 17+]:
echo    - %RELEASE_DIR%\cross-platform\%APP_NAME%.jar
echo.
echo  DISTRIBUTION ZIPs:

for %%F in ("%RELEASE_DIR%\%APP_NAME%-%APP_VERSION%-windows.zip") do (
    set /a SIZE_MB=%%~zF/1024/1024
    echo    - %APP_NAME%-%APP_VERSION%-windows.zip [!SIZE_MB! MB]
)

for %%F in ("%RELEASE_DIR%\%APP_NAME%-%APP_VERSION%-windows-debug.zip") do (
    set /a SIZE_MB=%%~zF/1024/1024
    echo    - %APP_NAME%-%APP_VERSION%-windows-debug.zip [!SIZE_MB! MB]
)

for %%F in ("%RELEASE_DIR%\%APP_NAME%-%APP_VERSION%-cross-platform.zip") do (
    set /a SIZE_KB=%%~zF/1024
    echo    - %APP_NAME%-%APP_VERSION%-cross-platform.zip [!SIZE_KB! KB]
)

echo.
echo ============================================
goto :end

:error
echo.
echo ============================================
echo  BUILD FAILED!
echo ============================================

:end
echo.
pause