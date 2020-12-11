# Spark Assessment
================

## About
-----

This application parses UK Crime Data files and 
provides the user with an interface to extract information from these files.

## Setup
-----
The application is packaged with sbt-assembly in a fat jar file:

	UKCrimeDataWithSpark.jar
	
Please ensure you have java 1.8 (see below)

	PS C:\Users\Lucian\Desktop\My App> java -version
	java version "1.8.0_271"
	Java(TM) SE Runtime Environment (build 1.8.0_271-b09)
	Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, mixed mode)

All you have to do to launch the app is to either run the command:		

	java -jar .\UKCrimeDataWithSpark.jar
	
or just launch UKCrimeDataWithSpark.bat (does the same thing).

It will now need some file to process, so please copy the CSV files in the same folder as the application,
under CSV_Resources/UK_CrimeData. It should look like this:

```
.
|____CSV_Resources
|    |
|    |____UK_CrimeData
|          |
|          |____2018-01
|          |     |
|          |     |  2018-01-avon-and-somerset-outcomes.csv
|          |     |  2018-01-avon-and-somerset-street.csv
|          |     |  ...
|          |
|          |____2018-02
|                |
|                |  2018-02-avon-and-somerset-outcomes.csv
|                |  2018-02-avon-and-somerset-street.csv
|                |  ...
|
| UKCrimeDataWithSpark.jar
| UKCrimeDataWithSpark.bat
```

## Run
-----
The application starts a sever at http://localhost:8080.

You will interact with the data through GET calls. I suggest using Postman to send requests.

Functionality is divided in two, writer and viewer. If you run GET on:

	http://localhost:8080/write/GetQueries
	http://localhost:8080/view/GetQueries
you will see available requests that you can perform.

The main job that parses input files is started by running, and **should be run first**.

	http://localhost:8080/write/ParseInitialFilesAndWriteParquetFile
It will generate the Result/result.parquet file that is used by the other requests.

You can then run some commands to view some data aggregations:

	http://localhost:8080/view/GetCrimeTypes
	http://localhost:8080/view/GetDistricts
	http://localhost:8080/view/GetCrimesForDistrict
or you can launch commands to write data to files:

	http://localhost:8080/write/WriteCrimeTypes
	http://localhost:8080/write/WriteDistricts
	http://localhost:8080/write/WriteCrimesByDistrict
	http://localhost:8080/write/WriteCrimesByCrimeType
These will write Result/*.json files.
	
All the file paths are configured in application.conf. 

## Technical Aspects
-----

The project is build using Akka framework to handle the HTTP communication and the interaction with Spark. I've taken the decision to go with Akka because I've imagined a microservice application where
different callers are able to schedule Spark jobs asynchronously. 

For the moment the application uses only GET requests, but it can be extended to handle messages exchanged between client and actors,
as Akka HTTP supports JSON marshalling, another reason for choosing it. You can find more about Akka and Akka HTTP in the links and examples below (it's what I've used to implement the application):

[Akka](https://www.lightbend.com/akka-platform)
[Akka Quickstart](https://developer.lightbend.com/guides/akka-quickstart-scala/)
[Akka HTTP Quickstart](https://developer.lightbend.com/guides/akka-http-quickstart-scala/index.html)

Also **I've never used Akka** and was curious to play around with the framework :).

### Basic acrhitecture and how it works:

When the app starts, it creates an ActorSystem with a Data Viewer Actor and a Data Writer Actor, and binds specific routes to a port, in this case, localhost:8080.

The routes are built using Akka DSL (domain specific language). As taken from the Akka example documentation, "a Route defines: the paths (/view, /write ...), the available HTTP methods, and when applicable, parameters or payloads."

When the endpoints are invoked, they will interact with the Writer Actor or the Viewer Actor.
Each actor knows how to handle specific commands. The actors have their own Parser instances that are used to further interact with Spark. I've implemented it this way so that I would be able to mock the Parsers when unit testing the actors.

### MainApp.scala

Holds the app main method. It binds the routes to the http server and launches it.
It also initiates the SparkSession so that we don't have to wait for it at when running the first request. 

### AppRoutes.scala

Defines the Akka HTTP Routes. It takes as constructor parameters the two Actors: the Writer and the Viewer. It binds the path endpoints to these actors.
E.g. http://localhost:8080/view/GetCrimesForDistrict?district=bedfordshire ask the Viewer to handle "GetCrimeForDistrict" command.

	dataViewerActor.ask(DataViewerActor.RunCommand(command, district, _)) 
	
Also, here the timeout duration is specified. If a request takes longer, it will timeout. And as Spark requests tend to not be that quick, the job is started and a reply is sent to the caller without waiting for the job to finish.
There are some Spark jobs that don't take that long ( GetCrimeTypes, GetDistricts ..) and the result is directly sent as the reply. Here it would be nice to use the JSON marshaller provided in the Akka HTTP framework for the replies.

	private implicit val timeout = Timeout(15.seconds)

### DataViewerActor.scala and DataWriterActor.scala

The Viewer and the Writer actors know how to handle specific request. They are called with specific commands and with a reference (ActorRef) to the caller, to whom they will reply.
They hold references to the parser objects, and they call on them to schedule spark jobs. They are the middleware layer between the server and the spark parsers.

### InputDataParser.scala and ProcessedDataParser.scala

These two are the Spark interactors. They hold the dataframe logic and are called to start spark jobs. The InputDataParser knows only how to read the input CSVs and to write to the parquet file.
The ProcessedDataParser reads the parquet file and either writes JSONs on processed data when interacting with the Writer Actor, or replies to the Viewer Actor.

### Testing

I've tried to extensively cover the code. There's unit tests for the routes and actors and integration tests for the parsers. 
There's a different configuration file for testing, so that I'm using different paths and test files. The environment should however be the same.

### Packaging

To package the app I've used SBT assembly. To enable this, I've added an assembly.sbt file in the project root with the following line that specifies the version for the plugin:

	addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
	
Also in the build.sbt file some configuration needed to be done in order to handle merge strategies, tell sbt to skip testing when packaging and to specify the main class: 

	assemblyMergeStrategy in assembly := {
	  case n if n.contains("services") => MergeStrategy.concat
	  case PathList("reference.conf") => MergeStrategy.concat
	  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
	  case x => MergeStrategy.first
	}

	test in assembly := {}

	mainClass in assembly := Some("MainApp")

### Worst day ever :) 

I've spent a day trying to understand why Spark was slow on this project but fast on an earlier project that had more or less the same code. I've went through join strategies, 
trying to eliminate null keys, trying to tweak the join keys, and I've even tried salting the keys, and nothing seemed to work.
	
The problem was that when I've added Akka to the project, there was a logging library dependecy that was also used by Spark, and somehow it was improperly set, making it synchronus. Coulprit below:
	
	"ch.qos.logback"    %  "logback-classic"          % "1.2.3",
	
This was fixed by adding the logback.xml configuration file, and properly configuring it. I don't want to hear about it ever again :). 

Thank you for going this far. 

Best regards, Lucian Radulescu.
	
	






 
