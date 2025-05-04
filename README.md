<h1 align="center"> XCSP3-Java-Tools v2.5.1 </h1>



This library provides:
  * a parser written in Java 8 for XCSP3 instances ; see [miniguide](doc/parserJava1-2.pdf)
  * a tool for checking solutions (and bounds) of XCSP3 instances ; see [miniguide](doc/checkerJava1-1.pdf) 

A C++ parser is available in its own [repository](https://github.com/xcsp3team/XCSP3-CPP-Parser).

A Python library for modeling, called PyCSP3, is also independently available. 
For the current version of PyCSP3, note that:
* a website including more than 60 Jupyter notebooks can be found at [pycsp.org](https://pycsp.org).
* the code is available on [Github](https://github.com/xcsp3team/pycsp3)
* a [well-documented guide](https://arxiv.org/abs/2009.00326) is available
* PyCSP3 is available as a PyPi package [here](https://pypi.org/project/pycsp3/)
* a GitHub repository with more than 340 models is available at [pycsp3-models](https://github.com/xcsp3team/pycsp3-models)

# 1) Documentation

Information about XCSP3 can be found at [xcsp.org](https://xcsp.org).

For details about these tools, see the documents referenced below. 

## Java Parser

See the miniguide in file [`parserJava1-2.pdf`](doc/parserJava1-2.pdf).

## Solution Checker

See the miniguide in file [`checkerJava1-1.pdf`](doc/checkerJava1-1.pdf).




# 2) Obtaining Binary and Sources of XCSP3 Tools 

## Maven Artifact

You can get it at the [Central Repository](https://search.maven.org).
Currently, this is:

    <dependency>
      <groupId>org.xcsp</groupId>
      <artifactId>xcsp3-tools</artifactId>
      <version>2.5.1</version>
    </dependency>

## Directly Building a JAR with Maven

1. Clone the repository : `git clone https://github.com/xcsp3team/XCSP3-Java-Tools.git`
1. Change directory : `cd XCSP3-Java-Tools`
1. Run Maven : `mvn package -Dmaven.test.skip=true`  (of course, you need Maven to be installed)
1. Test the JARs in the directory 'target'. For example, while choosing the right value for X-Y-Z,
  - `java -cp target/xcsp3-tools-X-Y-Z.jar org.xcsp.modeler.Compiler`. If the usage of the compiler is displayed, you are fine. 
  
 You can also directly hit a main method:
  - `java -jar target/xcsp3-compiler-X-Y-Z.jar` 
  - `java -jar target/xcsp3-solutionChecker-X-Y-Z.jar` 
  - `java -jar target/xcsp3-competitionValidator-X-Y-Z.jar` 

## Directly Building a JAR with Gradle

1. Clone the repository : `git clone https://github.com/xcsp3team/XCSP3-Java-Tools.git`
1. Change directory : `cd XCSP3-Java-Tools`
1. Run Gradle : `gradle build -x test`  (of course, you need Gradle to be installed)
1. Test the JAR : `java -cp build/libs/xcsp3-tools-X-Y-Z.jar org.xcsp.modeler.Compiler` (choose the right values for X-Y-Z)
If the usage of the compiler is displayed, you are fine. 

With this JAR, you can run the compiler (JvCSP3) and the solution checker. 
See details in the documents, referenced above.  
  
