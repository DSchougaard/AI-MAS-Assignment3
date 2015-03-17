#!/bin/bash
javac -cp . client/SearchClient.java
java -jar server.jar -l levels/SAtest.lvl -g 200 -c "java -Xmx2048 client.GuiClient"
