cd %~dp0
javac -cp . client/SearchClient.java
java -jar server.jar -l levels/MAbispebjerg.lvl -g 200 -c "java  -Xmx2048m client.SearchClient"

pause