object TestConstants {

  val HomeText = "Oh, hello there... you can try to run some of the queries below. Cheers, Lucian !\n\nhttp://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict \n\nor\n\nhttp://localhost:8080/write/ParseInitialFilesAndWriteParquetFile\nhttp://localhost:8080/write/WriteCrimeTypes\nhttp://localhost:8080/write/WriteDistricts\nhttp://localhost:8080/write/WriteCrimesByDistrict\nhttp://localhost:8080/write/WriteCrimesByCrimeType\nhttp://localhost:8080/write/GetQueries"

  val MockReplyViewer = "This is a mock reply from a Mock Viewer Actor"
  val MockReplyWriter = "This is a mock reply from a Mock Writer Actor"

  val ViewerQueries = "http://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict"
  val WriterQueries = "http://localhost:8080/write/ParseInitialFilesAndWriteParquetFile\nhttp://localhost:8080/write/WriteCrimeTypes\nhttp://localhost:8080/write/WriteDistricts\nhttp://localhost:8080/write/WriteCrimesByDistrict\nhttp://localhost:8080/write/WriteCrimesByCrimeType\nhttp://localhost:8080/write/GetQueries"

  val ShowQueriesViewerText = "Showing available queries below:\n\n" + ViewerQueries
  val ShowQueriesWriterText = "Showing available queries below:\n\n" + WriterQueries

  val UnknownCommandViewerText = "Received unknown command \nSomeCommand\nTry one of the available queries from below: \n\n" + ViewerQueries
  val UnknownCommandWriterText = "Received unknown command \nSomeCommand\nTry one of the available queries from below: \n\n" + WriterQueries

  val SparkAddress = "http://host.docker.internal:4040"

  val MockCrimeTypes        = "[Bicycle theft]\n[Public order]\n[Drugs]\n[Other crime]\n[Robbery]\n[Criminal damage and arson]\n[Theft from the person]\n[Shoplifting]\n[Burglary]\n[Other theft]\n[Possession of weapons]\n[Violence and sexual offences]\n[Vehicle crime]\n[Anti-social behaviour]"
  val MockDistricts         = "[dorset]\n[lancashire]\n[cheshire]\n[hampshire]\n[west yorkshire]\n[merseyside]"
  val MockCrimesForDistrict = "[northumbria,Violence and sexual offences,74899]\n[northumbria,Anti-social behaviour,60749]\n[northumbria,Public order,31576]\n[northumbria,Criminal damage and arson,30418]\n[northumbria,Other theft,21849]\n[northumbria,Shoplifting,21246]\n[northumbria,Burglary,13371]\n[northumbria,Vehicle crime,11332]\n[northumbria,Drugs,5420]\n[northumbria,Other crime,5103]\n[northumbria,Theft from the person,2640]\n[northumbria,Bicycle theft,2517]\n[northumbria,Possession of weapons,2272]\n[northumbria,Robbery,1366]"

  val ViewerResponseGetCrimeTypes        = "Received command and showing crime types...\n\n" + MockCrimeTypes
  val ViewerResponseGetDistricts         = "Received command and showing districts...\n\n" + MockDistricts
  val ViewerResponseGetCrimesForDistrict = "Received command and showing crimes for district...\n\n" + MockCrimesForDistrict

  val WriterResponseWriteCrimeTypes         = "Writing crime types to JSON file...\n\nCheck started jobs at " + SparkAddress
  val WriterResponseWriteDistricts          = "Writing districts names to JSON file...\n\nCheck started jobs at " + SparkAddress
  val WriterResponseWriteCrimesByDistrict   = "Writing crimes by district to JSON file...\n\nCheck started jobs at " + SparkAddress
  val WriterResponseWriteCrimesByCrimeType  = "Writing crimes by crime type to JSON file...\n\nCheck started jobs at " + SparkAddress

  // -- Spark Integration Tests
  val CrimeTypes = "[Public order]\n[Drugs]\n[Criminal damage and arson]\n[Other theft]\n[Violence and sexual offences]\n[Vehicle crime]"
  val Districts = "[avon and somerset]\n[bedfordshire]"
  val AllCrimes = "[avon and somerset,Other theft,2]\n[avon and somerset,Violence and sexual offences,2]\n[avon and somerset,Public order,1]\n[avon and somerset,Criminal damage and arson,1]\n[bedfordshire,Vehicle crime,3]\n[bedfordshire,Violence and sexual offences,2]\n[bedfordshire,Drugs,1]"
  val BedfordshireCrimes = "[bedfordshire,Vehicle crime,3]\n[bedfordshire,Violence and sexual offences,2]\n[bedfordshire,Drugs,1]"
}
