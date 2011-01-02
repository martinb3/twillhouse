#!/bin/bash
CLASSPATH=bin
CLASSPATH="$CLASSPATH:lib\scala-library.jar"
CLASSPATH="$CLASSPATH:lib\jline-0.9.94.jar"
CLASSPATH="$CLASSPATH:lib\twitter4j-core-2.1.8.jar"
CLASSPATH="$CLASSPATH:lib\twitter4j-media-support-2.1.8.jar"
CLASSPATH="$CLASSPATH:lib\commons-io-2.0.1.jar"
java -cp $CLASSPATH org.skillhouse.twitter.Twillhouse