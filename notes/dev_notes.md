# Andrew Developer Notes

## Dependencies

### KGraph & ArangoDB

Andrew depends on the KGraph library.  In turn, KGraph depends on ArangoDB.  There are
two main ways to run ArangoDB in support of Andrew, are:

### Start ArangoDB in Docker

	docker run -p 8529:8529 -e ARANGO_ROOT_PASSWORD=openSesame arangodb/arangodb:latest


Using Docker provides a simple approach for small cases, but tends to run slow for anything
larger than unit tests.  The issues likely causing the slow performance of ArangoDB on
Docker can be seen in the container log file as:


	WARNING [118b0] {memory} maximum number of memory mappings per process is 65530, which seems too low. it is recommended to set it to at least 512000


### Start ArangoDB as a Process

This approach runs the code at least an order of magnitude faster.


## Building the Project

### main build command

	mvn clean install
	
	mvn clean source:jar javadoc:jar install -DskipTests

### To build the uber jar, run:

	mvn clean install assembly:single

### To display available updates to Maven dependencies:
 (see also: https://www.baeldung.com/maven-dependency-latest-version)
 
	mvn versions:display-dependency-updates
	
## To build licensing information:

	mvn site


The results are written to [ ./java-project/target/site/dependencies.html ].



## To build Javadoc
	mvn javadoc:javadoc
	
## to generate sample graph diagram images (requires Graphviz)
	dot -Tsvg .\target\dot_files\exportDot_goodThought_goodDot.dot > .\target\dot_files\exportDot_goodThought_goodDot.svg

## Running the Example

The initial full example of Andrew can be run by executing the ExamplesSuite JUnit suite.  This suite consists of one test.  That test starts with a single seed thought of linear_growth_thought, and runs for 10 generations in an attempt to forecast the 6-month future price for a set of 3 stocks.

The example is limited in several ways; the limitations are necessary to allow the example to run on a single machine in a reasonable amount of time.  Additionally, the example's complexity is limited due to the difficulty in generating complex seed thoughts by hand-generating text files.  A future task of a graphical tool for generating seed thoughts (and other types of seed graphs) will open up the engine to start with a wider variety and quality of seed thoughts. 

