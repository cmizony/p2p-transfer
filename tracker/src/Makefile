CC=gcc
CFLAGS=-W -Wall -g `pkg-config --cflags --libs glib-2.0 --libs gthread-2.0`
LDFLAGS=-lpthread
EXEC=tracker

all: $(EXEC)

tracker: tracker.o  treatMessage.o compileExpressions.o
		$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

test: compileExpressions.o  treatMessage.o testMessages.o
		$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

tracker.o: tracker.h compileExpressions.h
compileExpressions.o : compileExpressions.h
treatMessage.o : treatMessage.h
tools.o : tools.h

%.o: %.c
		$(CC) -o $@ -c $< $(CFLAGS)

.PHONY: clean 

clean:
		rm *.o $(EXEC) test
