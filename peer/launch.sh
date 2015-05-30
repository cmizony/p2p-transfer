#!/bin/sh
mkdir downloads
mkdir downloads2
touch downloads/test
echo TestP2P > downloads/test
mkdir bin-test/
javac -cp lib/guava-11.0.2.jar -d bin-test/ -sourcepath src/ src/com/trackeirb/peer/PeerLauncher.java
java -cp bin-test:lib/guava-11.0.2.jar com.trackeirb.peer.PeerLauncher
