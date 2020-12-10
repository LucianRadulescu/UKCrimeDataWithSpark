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
+-- CSV_Resources
|   +-- UK_CrimeData
|   	+--2018-01
|			+--2018-01-avon-and-somerset-outcomes.csv
|			+--2018-01-avon-and-somerset-street.csv
|			...
|		+--2018-02
|			+--2018-02-avon-and-somerset-outcomes.csv
|			+--2018-02-avon-and-somerset-street.csv
|			...
+-- UKCrimeDataWithSpark.jar
+-- UKCrimeDataWithSpark.bat
```
