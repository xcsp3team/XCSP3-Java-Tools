This library provides:
  * a parser written in Java 8 for XCSP3 instances ; see [miniguide](doc/parserJava1-2.pdf)
  * a tool for checking solutions (and bounds) of XCSP3 instances ; see [miniguide](doc/checkerJava1-0.pdf) 
  * MCSP3, an API to build models that can be compiled into XCSP3 files ; see [miniguide](doc/modelerReleaseCandidateV1.pdf)


A revision of the library is planned by 25 January, 2018. This will include :
  * the javadoc being finalized (currently, some auxiliary methods and classes are not documented)
  * MCSP3, official release, with an updated documentation 

A C++ parser is available in its own [repository](https://github.com/xcsp3team/XCSP3-CPP-Parser).

Information about XCSP3 can be found at www.xcsp.org.

# Building a JAR

1. clone the repository : `git clone https://github.com/xcsp3team/XCSP3-Java-Tools.git`
2. change directory : `cd XCSP3-Java-Tools`
3. run Gradle : `gradle build -x test`  (of course, you need Gradle to be installed)
4. test the JAR : `java -cp build/libs/toolX-YY-MM.jar org.xcsp.modeler.Compiler` (choose the right value for YY-MM)
If the usage of the compiler is displayed, you are fine. 

With this JAR, you can run the compiler (MCSP3) and the solution checker. 
See details in the documents, referenced below. 

# XCSP3-Java-Parser

See the miniguide in file [`parserJava1-2.pdf`](doc/parserJava1-2.pdf).

# XCSP3-Java-Solution-Checker

See the miniguide in file [`checkerJava1-0.pdf`](doc/checkerJava1-0.pdf).

# MCSP3 (modeler), Release Candidate

See the miniguide in file [`modelerReleaseCandidateV1.pdf`](doc/modelerReleaseCandidateV1.pdf). Official Release by 15 December 2017.
