#!/bin/bash

ROBOCODE_HOME=~/robocode
ENEMY_ROBOT=sample.VelociRobot

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

        # compile the trainer
        compile "sergey/lavrenyuk/nn/training/Trainer.java"

        # compile tests
        compile "sergey/lavrenyuk/test/*.java"
    elif [ $1 == "clean" ]
    then
        clean
    elif [ $1 == "test" ]
    then
        execute "sergey.lavrenyuk.test.Runner"
    elif [ $1 == "training" ]
    then
        execute "sergey.lavrenyuk.nn.training.Trainer"
    elif [ $1 == "scoring" ]
    then
        # remind user to check 'config.properties'
        printf "\nVerify 'config.properties' file:\n"
        printf "    neuralNetwork.mode=scoring\n"
        printf "    scoring.roundsPerMatrix value is correct\n"

        # read number of rounds to run
        read -p "Rounds = " rounds

        # create battle file with rounds to run and enemy robot class
        sed -E "s/{NUM_ROUNDS}/$rounds/; s/{ENEMY_ROBOT}/$ENEMY_ROBOT/" $ROBOCODE_HOME/robots/sergey/lavrenyuk/data/template.battle > $ROBOCODE_HOME/battles/sergey_lavrenyuk.battle

        # run Robocode without UI. it has to be launched from its root directory. copied from $ROBOCODE_HOME/robocode.sh
        work_dir=`pwd`
        cd $ROBOCODE_HOME
        java -Xmx512M -classpath libs/robocode.jar -XX:+IgnoreUnrecognizedVMOptions "--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED" "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" "--add-opens=java.desktop/sun.awt=ALL-UNNAMED" robocode.Robocode -battle sergey_lavrenyuk.battle -nodisplay -nosound -tps 10000
        cd "$work_dir"

        # delete battle file
        rm $ROBOCODE_HOME/battles/sergey_lavrenyuk.battle
    else
        print_usage
    fi
else
    print_usage
fi
