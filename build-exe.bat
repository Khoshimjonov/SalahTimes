@echo off
setlocal enabledelayedexpansion

echo ============================================
echo  SalahTimesWidget - EXE Builder
echo ============================================
echo.

:: Configuration
set APP_NAME=SalahTimesWidget
set APP_VERSION=2.0.0
set MAIN_CLASS=uz.khoshimjonov.Main
set VENDOR=Khoshimjonov
set DESCRIPTION=Prayer Times Desktop Widget

:: Check JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set
    exit /b 1
)

echo Using Java: %JAVA_HOME%
"%JAVA_HOME%\bin\java" -version
echo.

:: Step 1: Clean old builds
echo [1/7] Cleaning previous builds...
if exist "build-output" rmdir /s /q "build-output"
if exist "dist" rmdir /s /q "dist"
if exist "dist-debug" rmdir /s /q "dist-debug"
echo Done.
echo.

:: Step 2: Build with Maven
echo [2/7] Building project with Maven...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed
    exit /b 1
)
echo Maven build completed.
echo.

:: Step 3: Create isolated input directory
echo [3/7] Preparing input files...
mkdir "build-output\input"
copy "target\%APP_NAME%.jar" "build-output\input\" >nul
if errorlevel 1 (
    echo ERROR: Could not find target\%APP_NAME%.jar
    echo.
    echo Available files in target:
    dir /b target\*.jar
    exit /b 1
)
echo Done.
echo.

:: Step 4: Create custom JRE
echo [4/7] Creating custom JRE with jlink...
"%JAVA_HOME%\bin\jlink" ^
    --add-modules java.base,java.desktop,java.logging,java.prefs,java.net.http,jdk.crypto.ec,jdk.localedata ^
    --include-locales=en,ru,uz ^
    --strip-debug ^
    --no-man-pages ^
    --no-header-files ^
    --compress=zip-6 ^
    --output "build-output\runtime"

if errorlevel 1 (
    echo ERROR: jlink failed
    exit /b 1
)
echo Done.
echo.

:: Step 5: Check for icon
set ICON_OPTION=
if exist "src\main\resources\images\main.ico" (
    set ICON_OPTION=--icon "src\main\resources\images\main.ico"
    echo Using custom icon.
) else (
    echo No .ico file found, using default icon.
)
echo.

:: Step 6: Create PRODUCTION exe (no console)
echo [5/7] Creating PRODUCTION build...
echo Please wait, this takes 30-60 seconds...
echo.

"%JAVA_HOME%\bin\jpackage" ^
    --verbose ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --input "build-output\input" ^
    --main-jar "%APP_NAME%.jar" ^
    --main-class "%MAIN_CLASS%" ^
    --runtime-image "build-output\runtime" ^
    --dest "build-output\production" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --description "%DESCRIPTION%" ^
    %ICON_OPTION% ^
    --java-options "-Xmx256m" ^
    --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
    echo ERROR: jpackage failed for production build
    exit /b 1
)
echo Production build done.
echo.

:: Step 7: Create DEBUG exe (with console)
echo [6/7] Creating DEBUG build...

"%JAVA_HOME%\bin\jpackage" ^
    --verbose ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --input "build-output\input" ^
    --main-jar "%APP_NAME%.jar" ^
    --main-class "%MAIN_CLASS%" ^
    --runtime-image "build-output\runtime" ^
    --dest "build-output\debug" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --description "%DESCRIPTION% (Debug)" ^
    %ICON_OPTION% ^
    --win-console ^
    --java-options "-Xmx256m" ^
    --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
    echo ERROR: jpackage failed for debug build
    exit /b 1
)
echo Debug build done.
echo.

:: Step 8: Copy to final folders
echo [7/7] Copying to output folders...
mkdir "dist"
mkdir "dist-debug"
xcopy /e /i /q "build-output\production\%APP_NAME%" "dist\%APP_NAME%"
xcopy /e /i /q "build-output\debug\%APP_NAME%" "dist-debug\%APP_NAME%"

echo.
echo ============================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ============================================
echo.
echo PRODUCTION (no console):
echo   dist\%APP_NAME%\%APP_NAME%.exe
echo.
echo DEBUG (with console for logs):
echo   dist-debug\%APP_NAME%\%APP_NAME%.exe
echo.
echo For distribution: zip the "dist\%APP_NAME%" folder
echo.

pause