#!/usr/bin/env sh
echo "Current directory is $(pwd)"
echo "\n=== SUREFIRE REPORTS ===\n"

for F in molr-mole-core/build/test-results/test/*.xml
do
    echo $F
    cat $F
    echo
done
