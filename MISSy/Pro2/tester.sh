#!/bin/bash

rm -f app
g++ miss2.cpp -o app -lgmp

for i in {1..10}
do
	./app 16 < "testy/d$i" > "/tmp/T$i"
	diff "/tmp/T$i" "testy/W$i"
done
