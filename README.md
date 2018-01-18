This library provides:
  * a parser written in Java 8 for XCSP3 instances ; see [miniguide](doc/parserJava1-2.pdf)
  * a tool for checking solutions (and bounds) of XCSP3 instances ; see [miniguide](doc/checkerJava1-0.pdf) 
  * MCSP3, an API to build models that can be compiled into XCSP3 files ; see [miniguide](doc/modelerReleaseCandidateV1.pdf)


A revision of the library is planned by 25 January, 2018. This will include :
  * the javadoc being finalized (currently, some auxiliary methods and classes are not documented)
  * MCSP3, official release, with an updated documentation 

A C++ parser is available in its own [repository](https://github.com/xcsp3team/XCSP3-CPP-Parser).

1. Documentation

Information about XCSP3 can be found at www.xcsp.org.

For details about these tools, see the documents referenced below. 

## XCSP3-Java-Parser

See the miniguide in file [`parserJava1-2.pdf`](doc/parserJava1-2.pdf).

## XCSP3-Java-Solution-Checker

See the miniguide in file [`checkerJava1-0.pdf`](doc/checkerJava1-0.pdf).

## MCSP3 (modeler), Release Candidate

See the miniguide in file [`modelerReleaseCandidateV1.pdf`](doc/modelerReleaseCandidateV1.pdf). 

1. Obtaining Binary and Sources of XCSP3 Tools 

## Referring to a Maven Artifact (Release)

You can get the following information in the [Central Repository](http://search.maven.org):

`<dependency>
  <groupId>org.xcsp</groupId>
  <artifactId>xcsp3-tools</artifactId>
  <version>0.9.0</version>
</dependency>`

## Just Building a JAR with Maven

1. clone the repository : `git clone https://github.com/xcsp3team/XCSP3-Java-Tools.git`
2. change directory : `cd XCSP3-Java-Tools`
3. run Maven : `mvn package -Dmaven.test.skip=true`  (of course, you need Maven to be installed)
4. test the JARs in the directory 'target'. For example, while choosing the right value for X-Y-Z,
  - `java -cp target/xcsp3-tools-X-Y-Z.jar org.xcsp.modeler.Compiler`. If the usage of the compiler is displayed, you are fine. 
  - `java -jar target/xcsp3-solchecker-X-Y-Z.jar` 
  - `java -jar target/xcsp3-compiler-X-Y-Z.jar` 

## Just Building a JAR with Gradle

1. clone the repository : `git clone https://github.com/xcsp3team/XCSP3-Java-Tools.git`
2. change directory : `cd XCSP3-Java-Tools`
3. run Gradle : `gradle build -x test`  (of course, you need Gradle to be installed)
4. test the JAR : `java -cp build/libs/xcsp3-tools-X-Y-Z.jar org.xcsp.modeler.Compiler` (choose the right values for X-Y-Z)
If the usage of the compiler is displayed, you are fine. 

With this JAR, you can run the compiler (MCSP3) and the solution checker. 
See details in the documents, referenced below. 
  