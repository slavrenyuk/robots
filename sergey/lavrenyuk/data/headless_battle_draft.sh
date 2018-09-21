#!/bin/bash

if [ $# -eq 0 ]
then
    sed -E "s/a/a/" perceptron-velocirobot.battle
else
    sed -E "s/numRounds=[0-9]+/numRounds=$1/" perceptron-velocirobot.battle > current.battle
fi
