cd %~dp0
del /s /q /f *.class
cls
javac -cp . client/SearchClient.java
java -jar cserver.jar -d complevels/ -c "java client.SearchClient"

pause