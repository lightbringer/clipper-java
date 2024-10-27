**Deprecation notice Oct 2024:** I wrote this code many years ago and stopped maintaining it a long time ago. But since it still seems very popular, and I get the occasional email about it: 

> [!NOTE]
> There exists a verion 2.0 of the orignal library, and someone already made a [Java port](https://github.com/micycle1/Clipper2-java/) of that. 

Original readme below:

# clipper-java
A Polygon clipper for Java

Polygon Clipper is a library to execute various boolean operations (Union, Difference, XOR, etc.) on arbitrary 2D polygons, e.g. calculate the area in which two polygons overlap. It comes with two Demo applications, one for the console and one using a Swing based GUI.

It’s a 1:1 Java portation of the Clipper project developed by <a href="http://www.angusj.com/delphi/clipper.php">Angus Johnson</a>, which as an implementation of the algorithm proposed by <a href="http://en.wikipedia.org/wiki/Vatti_clipping_algorithm">Bala R. Vatti</a>. I just converted the code to Java 8, based on his C# Clipper version 6.4.2. If you’re interested in the documentation or have questions regarding the algorithm, please head over to his homepage. However, feel free to contact me if you found a bug or have a question regarding the Java version.
