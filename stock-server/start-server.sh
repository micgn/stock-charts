#!/bin/sh

/root/jdk1.8.0_73/bin/java -Dswarm.http.port=56075 -jar stock-server-1.0-SNAPSHOT-swarm.jar

# /root/jdk1.8.0_73/bin/java -Dswarm.http.port=56075 -DmailHost=localhost -DmailFrom=stock@trali.de -DmailTo=mgn1@trali.de -jar stock-server-1.0-SNAPSHOT-swarm.jar

# /root/jdk1.8.0_73/bin/java -Dswarm.http.port=56075 -DwriteAccessEnabled -jar stock-server-1.0-SNAPSHOT-swarm.jar