#!/bin/bash -x

# java -jar [jarFileName.jar] [MI] [threshold] [NI] [alpha] [dataset] [nRows] [nColumns] [cores] [random/optimized] > [outputFileName.txt]


###MP###
for j in 1 2 3 4 5 6 7 8 9 10
do
echo "ITERATION $j START"

# changing dataset for default values
for i in 5 6 7 13 8 9 10 11
do
    echo "[Sequential] Running dataset $i . . ."
    java -jar MP.jar 40 250000000 50 0.9 $i 0
done

echo "ITERATION $j END"
done


for j in 1 2 3 4 5 6 7 8 9 10
do
echo "ITERATION $j START"

# changing maxItr for 70K
for i in 20 30 50 60
do
    echo "[Sequential] Running dataset 70K with maxItr $i . . ."
    java -jar MP.jar $i 250000000 50 0.9 11 0
done

echo "ITERATION $j END"
done


for j in 1 2 3 4 5 6 7 8 9 10
do
echo "ITERATION $j START"

# changing threshold for 70K
for i in 50000000 150000000 375000000 500000000
do
    echo "[Sequential] Running dataset 70K with threshold $i . . ."
    java -jar MP.jar 40 $i 50 0.9 11 0
done

echo "ITERATION $j END"
done


for j in 1 2 3 4 5 6 7 8 9 10
do
echo "ITERATION $j START"

# changing convSA for 70K
for i in 10 30 70 90
do
    echo "[Sequential] Running dataset 70K with convSA $i . . ."
    java -jar MP.jar 40 250000000 $i 0.9 11 0
done

echo "ITERATION $j END"
done


for j in 1 2 3 4 5 6 7 8 9 10
do
echo "ITERATION $j START"

# changing alpha for 70K
for i in 0.998
do
    echo "[Sequential] Running dataset 70K with alpha $i . . ."
    java -jar MP.jar 40 250000000 50 $i 11 0
done

echo "ITERATION $j END"
done



