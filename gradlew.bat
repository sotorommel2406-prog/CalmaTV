@echo off
setlocal
set APP_HOME=%~dp0
if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if not exist "%CLASSPATH%" (
    echo ERROR: falta gradle\wrapper\gradle-wrapper.jar ^(ver README.md^).
    exit /b 1
)
"%JAVA_EXE%" -Xmx64m -Xms64m "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
