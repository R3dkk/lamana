@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo JDK tidak ditemukan. Install JDK 21 lalu coba lagi.
    pause
    exit /b 1
)
where jar >nul 2>nul
if errorlevel 1 (
    echo Tool jar tidak ditemukan. Pastikan JDK 21 ada di PATH.
    pause
    exit /b 1
)

if not exist target\classes mkdir target\classes
javac --release 8 -encoding UTF-8 -d target\classes -sourcepath src\main\java src\main\java\lamana\Main.java
if errorlevel 1 (
    echo Build gagal. Periksa pesan error di atas.
    pause
    exit /b 1
)

xcopy /E /I /Y "src\main\resources\*" "target\classes\" >nul
jar cfe target\animal-war-1.0.0.jar lamana.Main -C target\classes .
if errorlevel 1 (
    echo Pembuatan JAR gagal.
    pause
    exit /b 1
)

start "" javaw -jar target\animal-war-1.0.0.jar
endlocal
