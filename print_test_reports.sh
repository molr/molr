#!/usr/bin/env sh
echo "Current directory is $(pwd)"
echo "\n=== SUREFIRE REPORTS ===\n"

for F in molr-commons/target/surefire-reports/*.txt
do
    echo $F
    cat $F
    echo
done

for F in molr-mole-core/target/surefire-reports/*.txt
do
    echo $F
    cat $F
    echo
done

for F in molr-mole-server/target/surefire-reports/*.txt
do
    echo $F
    cat $F
    echo
done

for F in molr-mole-remote/target/surefire-reports/*.txt
do
    echo $F
    cat $F
    echo
done
