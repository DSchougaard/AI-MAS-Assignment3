cd %~dp0
java -jar server.jar -l levels/MAchallenge.lvl -g 200 -c "java -jar -Xmx2048m client.jar"

pause