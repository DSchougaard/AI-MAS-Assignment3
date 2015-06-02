cd %~dp0
del /s /q /f *.class
cls
javac -cp . client/SearchClient.java
java -jar server.jar -l levels/MAbispebjerg.lvl -g 200 -c "java  -Xmx2048m client.SearchClient -dm FloydWarshallDistanceMap"

pause