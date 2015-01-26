#!/bin/bash
if [ "$#" -eq  "1" ] 
	then
	mkdir output
	cd $1
	for file in *.tar.gz
	do
  		tar -vxzf "$file" -C "../output"
	done
	cd ../output
	ls | grep -v .csv$| xargs rm
else
echo "usage: ./untar <target folder>"
fi
