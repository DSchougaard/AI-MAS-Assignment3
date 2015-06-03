cd %~dp0
java -jar cserver.jar -d levels/ -c "java -jar -Xmx2048m  client.jar"

pause