#!/bin/bash
ROBOCODE_HOME=~/robocode
MY_ROBOTS_HOME=$ROBOCODE_HOME/robots/sergey/lavrenyuk
javac -source 1.8 -target 1.8 -classpath $ROBOCODE_HOME/libs/robocode.jar:$ROBOCODE_HOME/robots/ $ROBOCODE_HOME/robots/sergey/lavrenyuk/**/*.java $ROBOCODE_HOME/robots/sergey/lavrenyuk/*.java
