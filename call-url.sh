#!/bin/bash
i=0
while [ $i -le 10000 ]
do
  echo $i
  let i=$i+1
  echo `curl http://localhost:9000/hello-world`
  sleep 1
done