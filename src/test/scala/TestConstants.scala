object TestConstants {

  val homeText = "Oh, hello there... you can try to run some of the queries below. Cheerios, Lucian !\n\nhttp://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict \n\nor\n\nhttp://localhost:8080/write/ParseInitialFilesAndWriteParquetFile\nhttp://localhost:8080/write/WriteCrimeTypes\nhttp://localhost:8080/write/WriteDistricts\nhttp://localhost:8080/write/WriteCrimesByDistrict\nhttp://localhost:8080/write/WriteCrimesByCrimeType\nhttp://localhost:8080/write/GetQueries"

  val mockReplyViewer = "This is a mock reply from a Mock Viewer Actor"
  val mockReplyWriter = "This is a mock reply from a Mock Writer Actor"

  val viewerQueries = "http://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict"
  val writerQueries = "http://localhost:8080/write/ParseInitialFilesAndWriteParquetFile\nhttp://localhost:8080/write/WriteCrimeTypes\nhttp://localhost:8080/write/WriteDistricts\nhttp://localhost:8080/write/WriteCrimesByDistrict\nhttp://localhost:8080/write/WriteCrimesByCrimeType\nhttp://localhost:8080/write/GetQueries"

  val showQueriesViewerText = "Showing available queries below:\n\n" + viewerQueries
  val showQueriesWriterText = "Showing available queries below:\n\n" + writerQueries

  val unknownCommandViewerText = "Received unknown command \nSomeCommand\nTry one of the available queries from below: \n\n" + viewerQueries
  val unknownCommandWriterText = "Received unknown command \nSomeCommand\nTry one of the available queries from below: \n\n" + writerQueries

  val sparkAddress = "http://host.docker.internal:4040"

  val mockCrimeTypes        = "[Bicycle theft]\n[Public order]\n[Drugs]\n[Other crime]\n[Robbery]\n[Criminal damage and arson]\n[Theft from the person]\n[Shoplifting]\n[Burglary]\n[Other theft]\n[Possession of weapons]\n[Violence and sexual offences]\n[Vehicle crime]\n[Anti-social behaviour]"
  val mockDistricts         = "[dorset]\n[lancashire]\n[cheshire]\n[hampshire]\n[west yorkshire]\n[merseyside]"
  val mockCrimesForDistrict = "[northumbria,Violence and sexual offences,74899]\n[northumbria,Anti-social behaviour,60749]\n[northumbria,Public order,31576]\n[northumbria,Criminal damage and arson,30418]\n[northumbria,Other theft,21849]\n[northumbria,Shoplifting,21246]\n[northumbria,Burglary,13371]\n[northumbria,Vehicle crime,11332]\n[northumbria,Drugs,5420]\n[northumbria,Other crime,5103]\n[northumbria,Theft from the person,2640]\n[northumbria,Bicycle theft,2517]\n[northumbria,Possession of weapons,2272]\n[northumbria,Robbery,1366]"

  val viewerResponseGetCrimeTypes        = "Received command and showing crime types...\n\n" + mockCrimeTypes
  val viewerResponseGetDistricts         = "Received command and showing districts...\n\n" + mockDistricts
  val viewerResponseGetCrimesForDistrict = "Received command and showing crimes for district...\n\n" + mockCrimesForDistrict

  val writerResponseWriteCrimeTypes         = "Writing crime types to JSON file...\n\nCheck started jobs at " + sparkAddress
  val writerResponseWriteDistricts          = "Writing districts names to JSON file...\n\nCheck started jobs at " + sparkAddress
  val writerResponseWriteCrimesByDistrict   = "Writing crimes by district to JSON file...\n\nCheck started jobs at " + sparkAddress
  val writerResponseWriteCrimesByCrimeType  = "Writing crimes by crime type to JSON file...\n\nCheck started jobs at " + sparkAddress

}
