# clipper-java
A Polygon clipper for Java

Polygon Clipper is a library to execute various boolean operations (Union, Difference, XOR, etc.) on arbitrary 2D
polygons, e.g. calculate the area in which two polygons overlap. It comes with two Demo applications, one for the
console and one using a Swing based GUI.

It’s a 1:1 Java portation of the Clipper project developed by
<a href="http://www.angusj.com/delphi/clipper.php">Angus Johnson</a>, which as an implementation of the algorithm
proposed by <a href="http://en.wikipedia.org/wiki/Vatti_clipping_algorithm">Bala R. Vatti</a>.
I just converted the code to Java 8, based on his C# Clipper version 6.4.2. If you’re interested in the documentation
or have questions regarding the algorithm, please head over to his homepage. However, feel free to contact me if you
found a bug or have a question regarding the Java version.

## Release Notes

### 6.4.2.1-SNAPSHOT

- Use long instead of double for coordinates.

### Prerequisites

clipper-java requires Java 8.

### Installing

The project can be installed in the local repository using Maven.
From the base directory, type:

```
mvn clean install
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## License

This project is licensed under the Boost Software License - Version 1.0 - August 17th, 2003 - see the license.txt
file for details
