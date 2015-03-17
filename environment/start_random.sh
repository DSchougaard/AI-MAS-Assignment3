#!/bin/bash
javac -cp . client/RandomWalkClient.java
java -jar server.jar -l levels/SAtest.lvl -g 200 -c "java  -Xmx2048m client.RandomWalkClient"