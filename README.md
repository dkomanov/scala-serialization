### Scala serialization [![Build Status](https://travis-ci.org/dkomanov/scala-serialization.svg?branch=master)](https://travis-ci.org/dkomanov/scala-serialization)

A source code for the article "Scala Serialization" at [medium](https://medium.com/@dkomanov/scala-serialization-419d175c888a).

Recent charts for the article is at https://dkomanov.github.io/scala-serialization/.

To build and run benchmarks use the following command:

```sbt
sbt clean 'scala-serialization-test/jmh:run -prof gc -rf json -rff jmh-result.json .*'
```
