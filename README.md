Spark Assessment
================

About
-----

This project is an application that parses UK Crime Data files and 
provides the user with an interface to extract information from these files.

Setup
-----
The application is packaged with sbt-assemly in a fat jar file:
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

Run
-----
The application starts a sever at http://localhost:8080.
You will interact with the data through GET calls. I suggest using Postman to send requests.

Functionality is divided in two, writer and viewer. If you run GET on:
	http://localhost:8080/write/GetQueries
	http://localhost:8080/view/GetQueries
you will see available requests that you can perform.

The main job that parses input file is started by running, and **should run first**.
	http://localhost:8080/write/ParseInitialFilesAndWriteParquetFile
It will generate the Result/result.parquet file that is used by the other requests.

You can then run some view commands to view some data aggregations:
	http://localhost:8080/view/GetCrimeTypes
	http://localhost:8080/view/GetDistricts
	http://localhost:8080/view/GetCrimesForDistrict
or you can launch some commands to write data aggregations to files:
	http://localhost:8080/write/WriteCrimeTypes
	http://localhost:8080/write/WriteDistricts
	http://localhost:8080/write/WriteCrimesByDistrict
	http://localhost:8080/write/WriteCrimesByCrimeType
These will write Result/*.json files
	
All the file paths are configured in application.conf. 

Technical Aspects
-----

