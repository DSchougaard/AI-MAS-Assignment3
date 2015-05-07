FILENAME=report

ifeq ($(shell uname -s), Linux)
	OPEN_UTIL = gnome-open
else
	OPEN_UTIL = open
endif

all: 
	latexmk -pdf $(FILENAME).tex
	texcount $(FILENAME).tex -inc -letter -total

clean:
	cp $(FILENAME).tex _$(FILENAME).tex
	rm $(FILENAME).*
	mv _$(FILENAME).tex $(FILENAME).tex

open:
	$(OPEN_UTIL) $(FILENAME).pdf



