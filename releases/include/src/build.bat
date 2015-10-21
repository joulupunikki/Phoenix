rem Phoenix Windows 7 build batch file
rem needs maven to be present in apache-maven-X.Y.Z directory
copy Phoenix\per-developer-pom.xml.STATIC Phoenix\per-developer-pom.xml
rem jre:s don't have jar
rem jar -xvf apache-maven-*.zip
mkdir apache-maven
cd apache-maven-*
xcopy /E /Y * ..\apache-maven
cd ..
cd math
call ..\util.bat
cd ..
cd Phoenix
call ..\util.bat
copy target\Phoenix.jar ..\..
cd ..
