cd %~dp0
cd ..
del /s /q /f *.class
cls
javac -cp . client/SearchClient.java
java -jar server.jar -l complevels/MAnull.lvl -g 200 -c "java  -Xmx2048m client.SearchClient"

pause