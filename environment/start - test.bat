cd %~dp0
del /s /q /f *.class
cls
javac -cp . client/SearchClient.java
java -jar cserver.jar -d test/ -c "java client.SearchClient"

pause