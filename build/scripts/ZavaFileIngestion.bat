@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  ZavaFileIngestion startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables, and ensure extensions are enabled
setlocal EnableExtensions

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and ZAVA_FILE_INGESTION_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\ZavaFileIngestion.jar;%APP_HOME%\lib\azure-identity-1.18.3.jar;%APP_HOME%\lib\azure-messaging-servicebus-7.17.18.jar;%APP_HOME%\lib\mssql-jdbc-12.8.2.jre8.jar;%APP_HOME%\lib\azure-core-http-netty-1.16.4.jar;%APP_HOME%\lib\azure-core-amqp-2.11.4.jar;%APP_HOME%\lib\azure-core-1.58.0.jar;%APP_HOME%\lib\azure-json-1.5.1.jar;%APP_HOME%\lib\azure-xml-1.2.1.jar;%APP_HOME%\lib\msal4j-persistence-extension-1.3.0.jar;%APP_HOME%\lib\msal4j-1.15.1.jar;%APP_HOME%\lib\jna-platform-5.17.0.jar;%APP_HOME%\lib\oauth2-oidc-sdk-11.9.1.jar;%APP_HOME%\lib\json-smart-2.5.0.jar;%APP_HOME%\lib\qpid-proton-j-extensions-1.2.6.jar;%APP_HOME%\lib\slf4j-api-1.7.36.jar;%APP_HOME%\lib\jackson-annotations-2.18.6.jar;%APP_HOME%\lib\jackson-core-2.18.6.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.18.6.jar;%APP_HOME%\lib\jackson-databind-2.18.6.jar;%APP_HOME%\lib\jna-5.17.0.jar;%APP_HOME%\lib\reactor-netty-http-1.2.16.jar;%APP_HOME%\lib\reactor-netty-core-1.2.16.jar;%APP_HOME%\lib\reactor-core-3.7.17.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.133.Final.jar;%APP_HOME%\lib\netty-codec-http2-4.1.133.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.133.Final.jar;%APP_HOME%\lib\netty-resolver-dns-native-macos-4.1.131.Final-osx-x86_64.jar;%APP_HOME%\lib\netty-resolver-dns-classes-macos-4.1.131.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.1.131.Final.jar;%APP_HOME%\lib\netty-handler-4.1.133.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.1.133.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.1.133.Final.jar;%APP_HOME%\lib\netty-codec-4.1.133.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.132.Final-linux-x86_64.jar;%APP_HOME%\lib\netty-transport-native-kqueue-4.1.132.Final-osx-x86_64.jar;%APP_HOME%\lib\netty-transport-classes-epoll-4.1.132.Final.jar;%APP_HOME%\lib\netty-transport-classes-kqueue-4.1.132.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.133.Final.jar;%APP_HOME%\lib\netty-transport-4.1.133.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.133.Final.jar;%APP_HOME%\lib\netty-tcnative-boringssl-static-2.0.75.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.133.Final.jar;%APP_HOME%\lib\netty-common-4.1.133.Final.jar;%APP_HOME%\lib\proton-j-0.34.1.jar;%APP_HOME%\lib\nimbus-jose-jwt-9.37.3.jar;%APP_HOME%\lib\jcip-annotations-1.0-1.jar;%APP_HOME%\lib\content-type-2.3.jar;%APP_HOME%\lib\lang-tag-1.7.jar;%APP_HOME%\lib\accessors-smart-2.5.0.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\netty-tcnative-classes-2.0.75.Final.jar;%APP_HOME%\lib\asm-9.3.jar


@rem Execute ZavaFileIngestion
@rem endlocal doesn't take effect until after the line is parsed and variables are expanded
@rem which allows us to clear the local environment before executing the java command
endlocal & "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %ZAVA_FILE_INGESTION_OPTS%  -classpath "%CLASSPATH%" com.zavabank.fileingestion.Main %* & call :exitWithErrorLevel

:exitWithErrorLevel
@rem Use "%COMSPEC%" /c exit to allow operators to work properly in scripts
"%COMSPEC%" /c exit %ERRORLEVEL%
