# HL7 Validation Library

## Dependencies

The project has two external dependencies :

### xml-util

* __GitHub Repository__ : https://github.com/usnistgov/xml-util
* __Jar File Location__ : /dependencies/xml-util-2.1.0/xml-util-2.1.0.jar
* __POM File Location__ : /dependencies/xml-util-2.1.0/xml-util-2.1.0.pom
* __Maven__ :
    ```
    <groupId>gov.nist</groupId>
    <artifactId>xml-util</artifactId>
    <version>2.1.0</version>
    ```

### validation-report

* __GitHub Repository__ : https://github.com/usnistgov/validation-report
* __Jar File Location__ : /dependencies/validation-report-1.1.0/validation-report-1.1.0.jar
* __POM File Location__ : /dependencies/validation-report-1.1.0/validation-report-1.1.0.pom
* __Maven__ :
    ```
    <groupId>com.github.hl7-tools</groupId>
    <artifactId>validation-report</artifactId>
    <version>1.1.0</version>
    ```

## Installing JARs to local M2
1) Open terminal
2) Navigate to "dependencies" folder from root of the project (v2-validation)
3) Install xml-utils, run command :
    ```
    mvn install:install-file 
    -Dfile=xml-util-2.1.0/xml-util-2.1.0.jar
    -DpomFile=xml-util-2.1.0/xml-util-2.1.0.pom
    ```
4) Install validation-report, run command :
    ```
    mvn install:install-file 
    -Dfile=validation-report-1.1.0/validation-report-1.1.0.jar
    -DpomFile=validation-report-1.1.0/validation-report-1.1.0.pom
    ```

## Build :

1) First install Scala Build Tool (sbt) : http://www.scala-sbt.org/
2) Make sure you have installed the external dependencies to your maven repository
3) Run "sbt" command on project root directory
4) To run tests you can use "test" command
5) To build the tool you can use "compile" command
