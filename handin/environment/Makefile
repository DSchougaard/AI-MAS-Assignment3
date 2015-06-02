# Variables
JAVA_OPTS = ""
LEVEL_PREFIX = levels/
level = MAsimple2.lvl

ifeq ($(OS),Windows_NT)
else
    UNAME_S := $(shell uname -s)
    ifeq ($(UNAME_S),Linux)
        JAVA += -Dsun.java2d.opengl=true
    endif
endif
JAVA += -jar

all: 
	javac client/*.java
	javac client/*/*.java
	javac client/*/*/*.java
	javac client/*/*/*/*.java

map:
	javac client/node/map/*.java
	javac client/node/*.java
	javac client/ArgumentParser.java
	javac client/SettingsContainer.java

run:
	java $(JAVA) -jar server.jar -l $(LEVEL_PREFIX)$(level) -g 200 -c "java -Xmx2048m client.SearchClient -dm ManhattanDistanceMap"

bench:
	java $(JAVA) -jar server.jar -l $(LEVEL_PREFIX)$(level) -o ../report/benchmarks -c "java -Xmx2048m client.SearchClient -dm ManhattanDistanceMap"

comp:
	java $(JAVA) -jar cserver.jar -d complevels/ -c "java -Xmx2048m client.SearchClient -dm FloydWarshallDistanceMap"


count:
	sloc --details --format cli-table .

clean:	
	rm -f *.class
	rm -f */*.class
	rm -f */*/*.class
	rm -f */*/*/*.class