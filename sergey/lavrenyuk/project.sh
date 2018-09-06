#!/bin/bash
ROBOCODE_HOME=~/robocode
MY_ROBOTS_HOME=$ROBOCODE_HOME/robots/sergey/lavrenyuk

print_usage() {
    printf "Usage:\n"
    printf "    project.sh <COMMAND>\n"
    printf "Commands:\n"
    printf "    compile   compiles Java source files and places compiled files to the same folder as corresponding sources\n"
    printf "    clean     deletes all compiled files\n"
}

if [ $# -eq 1 ]
then
    if [ $1 == "compile" ]
    then
        javac -source 1.8 -target 1.8 -classpath $ROBOCODE_HOME/libs/robocode.jar:$ROBOCODE_HOME/robots/ $ROBOCODE_HOME/robots/sergey/lavrenyuk/**/*.java $ROBOCODE_HOME/robots/sergey/lavrenyuk/*.java
    elif [ $1 == "clean" ]
    then
        find $ROBOCODE_HOME/robots/sergey/lavrenyuk/ -name *.class -type f -delete
    else
        print_usage
    fi
else
    print_usage
fi
