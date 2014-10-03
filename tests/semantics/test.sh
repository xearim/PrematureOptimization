#!/bin/sh

runsemantics() {
  $(git rev-parse --show-toplevel)/run.sh -t inter $1
}

fail=0

for file in `dirname $0`/illegal/*; do
  for decaf in $file/*; do
    if runsemantics $decaf > /dev/null 2>&1; then
      echo "Illegal file $decaf semantic checked successfully.";
      fail=1
    fi
  done
  echo "passed all tests in $file, yay"
done

for file in `dirname $0`/legal/*; do
  if ! runsemantics $file; then
    echo "Legal file $file failed to pass semantic checks.";
    fail=1
  fi
done

exit $fail;
