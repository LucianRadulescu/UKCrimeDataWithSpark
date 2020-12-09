object TestConstants {

  val homeText = "Oh, hello there... you can try to run some of the queries below. Cheerios, Lucian !\n\nhttp://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict \n\nor\n\nhttp://localhost:8080/write/ParseInitialFilesAndWriteParquetFile\nhttp://localhost:8080/write/WriteCrimeTypes\nhttp://localhost:8080/write/WriteDistricts\nhttp://localhost:8080/write/WriteCrimesByDistrict\nhttp://localhost:8080/write/WriteCrimesByCrimeType\nhttp://localhost:8080/write/GetQueries"

  val mockReplyViewer = "This is a mock reply from a Mock Viewer Actor"
  val mockReplyWriter = "This is a mock reply from a Mock Writer Actor"

  val unknownCommandViewerText = "Received unknown command \nSomeCommand\nTry one of the available queries from below: \n\nhttp://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict"
  val unknownCommandWriterText = "Received unknown command \nSomeCommand\nTry one of the available queries from below: \n\nhttp://localhost:8080/write/ParseInitialFilesAndWriteParquetFile\nhttp://localhost:8080/write/WriteCrimeTypes\nhttp://localhost:8080/write/WriteDistricts\nhttp://localhost:8080/write/WriteCrimesByDistrict\nhttp://localhost:8080/write/WriteCrimesByCrimeType\nhttp://localhost:8080/write/GetQueries"

  val sparkAddress = "http://host.docker.internal:4040"

  val writerResponseWriteCrimeTypes         = "Writing crime types to JSON file...\n\nCheck started jobs at " + sparkAddress
  val writerResponseWriteDistricts          = "Writing districts names to JSON file...\n\nCheck started jobs at " + sparkAddress
  val writerResponseWriteCrimesByDistrict   = "Writing crimes by district to JSON file...\n\nCheck started jobs at " + sparkAddress
  val writerResponseWriteCrimesByCrimeType  = "Writing crimes by crime type to JSON file...\n\nCheck started jobs at " + sparkAddress

}
