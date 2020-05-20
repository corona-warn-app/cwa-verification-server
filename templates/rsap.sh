#!/bin/bash

if [ -z "$1" ]; then echo "give me a filename"; exit; fi
mv $1 x.x
cat x.x | sed "s/, SAP AG//g" > $1
rm x.x
