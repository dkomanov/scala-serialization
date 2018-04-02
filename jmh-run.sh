#!/bin/sh -e

#ec() {
#    echo $* 1>&2
#    $*
#}

# expecting a gh-pages symlink in a root directory
#GH_PAGES_PATH=`readlink gh-pages`
# ensure that git repository exists there
#ec git -C $GH_PAGES_PATH status > /dev/null

#mvn clean install

java -jar scala-serialization-test/target/benchmarks.jar -rf json -rff jmh-result.json > jmh.log

#ec mv jmh-result.json $GH_PAGES_PATH

#echo "Don't forget to push gh-pages!"
