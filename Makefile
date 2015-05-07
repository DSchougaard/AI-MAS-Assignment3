all: report program

report:
	cd report && make

program:
	cd environment && make