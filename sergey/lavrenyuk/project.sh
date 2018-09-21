#!/bin/bash

ROBOCODE_HOME=~/robocode

print_usage() {
    printf "Usage:\n"
    printf "    project.sh <COMMAND>\n"
    printf "Commands:\n"
    printf "    compile   compiles robot and other related Java source files and places compiled files to the same folder as corresponding sources\n"
    printf "    clean     deletes all compiled files\n"
    printf "    test      runs all tests under sergey/lavrenyuk/test directory\n"
    printf "    training  launches robot's neural network weight matrices training\n"
    printf "    scoring   TODO\n"
}

clean() {
    find $ROBOCODE_HOME/robots/sergey/lavrenyuk/ -name '*.class' -type f -delete
    rm -rf $ROBOCODE_HOME/robots/out/
}

# $1 = class to compile
compile() {
    javac -source 1.8 -target 1.8 -classpath $ROBOCODE_HOME/libs/robocode.jar -sourcepath $ROBOCODE_HOME/robots/ -d $ROBOCODE_HOME/robots/  $ROBOCODE_HOME/robots/$1
}

# $1 = class to execute
execute() {
    java -classpath $ROBOCODE_HOME/libs/robocode.jar:$ROBOCODE_HOME/robots/ $1
}

if [ $# -eq 1 ]
then
    if [ $1 == "compile" ]
    then
        # compile the robot
        compile "sergey/lavrenyuk/Perceptron.java"
        # compile tests, otherwise Robocode complains
        compile "sergey/lavrenyuk/test/*.java"
    elif [ $1 == "clean" ]
    then
        clean
    elif [ $1 == "test" ]
    then
        compile "sergey/lavrenyuk/test/*.java"
        execute "sergey.lavrenyuk.test.Runner"
    elif [ $1 == "training" ]
    then
        compile "sergey/lavrenyuk/nn/training/TrainerRunner.java"
        execute "sergey.lavrenyuk.nn.training.TrainerRunner"
    elif [ $1 == "scoring" ]
    then
        printf "\nVerify 'config.properties' file:\n"
        printf "    neuralNetwork.mode=scoring\n"
        printf "    scoring.roundsPerMatrix value is correct\n"

        read -p "Rounds = " rounds
        printf "Confirm and continue? Y/N\n"
        read input
    else
        print_usage
    fi
else
    print_usage
fi
