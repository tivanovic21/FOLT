#! /bin/bash
while IFS=" " read -r pid proces
do
  echo "Zaustavljam: $pid - ${proces}"
  kill -9 $pid
done < <(jps | grep $1)