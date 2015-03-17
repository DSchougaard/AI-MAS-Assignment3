cd %~dp0
javac -cp . client/SearchClient.java
java -jar server.jar -l levels/SAtest.lvl -g 200 -c "java  -Xmx2048m client.GuiClient"

pause