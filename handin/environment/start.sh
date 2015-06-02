#!/bin/bash
javac -cp . client/GuiClient.java
java -jar server.jar -l levels/SACrunch.lvl -g 200 -c "java  -Xmx2048m client.SearchClient -dm FloydWarshallDistanceMap"
