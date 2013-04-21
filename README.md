
LESS CSS Compiler
=================

A standalone LESS CSS compiler for Java, spun-off from my [LESS CSS Filter](https://github.com/ultraq/lesscss-filter).

 - Current version: 1.0
 - Released: 21 April 2013


Requirements
------------

 - Java 7


Installation
------------

### Standalone distribution
Copy the JAR from [the latest download bundle](http://www.ultraq.net.nz/downloads/programming/LessCSS Compiler 1.0.zip),
or build the project from the source code here on GitHub.

### For Maven and Maven-compatible dependency managers
Add a dependency to your project with the following co-ordinates:

 - GroupId: `nz.net.ultraq.lesscss`
 - ArtifactId: `lesscss-compiler`
 - Version: `1.0`


Usage
-----

[Start writing LESS!](http://lesscss.org/).

In your Java program, create a new `LessCSSCompiler` instance, then call any of
the `compile()` methods to convert your LESS input into CSS output, eg:

```java
LessCSSCompiler compiler = new LessCSSCompiler();

// String input/output
String css = compiler.compile("@some-color: #add65c; #content { border-right: 0.25em solid @some-color; }");

// File input/output
compiler.compile(new File("input.less"), new File("output.css"));
```

...and so on.


Changelog
---------

### 1.0
 - Split processor/compiler part from the filter project.
 - Update lesscss.js version to 1.3.3.
