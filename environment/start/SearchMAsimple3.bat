cd %~dp0
cd ..
del /s /q /f *.class
cls
javac -cp . client/MAsimple3.java
java -jar server.jar -l levels/MAchallenge.lvl -g 200 -c "java  -Xmx2048m client.SearchClient"

pause