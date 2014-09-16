#!/bin/bash

runscanner() {
    curdir=$PWD
    cd `dirname $1`
    $(git rev-parse --show-toplevel)/run.sh -t scan `basename $1`
    cd $curdir
}

fail=0

for file in `dirname $0`/input/*; do
  if command -v tempfile >/dev/null 2>&1; then
      output=`tempfile`
  else
      output=`mktemp -t tmp`
  fi
  runscanner $file > $output 2>&1;
  if ! diff -u $output `dirname $0`/output/`basename $file`.out; then
    let "fail += 1"
    echo "Failed test $file"
  fi
  rm $output;
done

exit $fail;
