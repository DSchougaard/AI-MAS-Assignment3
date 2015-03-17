#!/bin/bash
javac -cp . client/GuiClient.java
java -jar server.jar -l levels/SAtest.lvl -g 200 -c "java  -Xmx2048m client.GuiClient"
