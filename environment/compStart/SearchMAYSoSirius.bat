cd %~dp0
cd ..
del /s /q /f *.class
cls
javac -cp . client/SearchClient.java
java -jar server.jar -l complevels/MAYSoSirius.lvl -g 200 -c "java  -Xmx8192m client.SearchClient"

pause