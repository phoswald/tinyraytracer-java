# tinyraytracer-java

Understandable RayTracing in 305 lines of plain Java

This is a port of [tinyraytracer by ssloy](https://github.com/ssloy/tinyraytracer)

## Build and Run

    $ mvn clean verify
    $ java -cp target/tinyraytracer.jar com.github.phoswald.tinyraytracer.Raytracer

## GraalVM (using Docker)

    $ mvn clean verify
    $ docker run -it --rm \
      -v $(pwd)/target:/target \
      -w /target \
      oracle/graalvm-ce:1.0.0-rc15 \
      native-image -cp tinyraytracer.jar com.github.phoswald.tinyraytracer.Raytracer tinyraytracer
    $ ./target/tinyraytracer

## Result

![](https://raw.githubusercontent.com/phoswald/tinyraytracer-java/master/out.png)
