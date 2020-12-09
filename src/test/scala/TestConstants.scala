object TestConstants {

  val homeText = "Oh, hello there... you can try to run some of the queries below. Cheerios, Lucian !\n\nhttp://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict \n\nor\n\nhttp://localhost:8080/write/ParseInitialFilesAndWriteParquetFile\nhttp://localhost:8080/write/WriteCrimeTypes\nhttp://localhost:8080/write/WriteDistricts\nhttp://localhost:8080/write/WriteCrimesByDistrict\nhttp://localhost:8080/write/WriteCrimesByCrimeType\nhttp://localhost:8080/write/GetQueries"

  val mockReplyViewer = "This is a mock reply from a Mock Viewer Actor"
  val mockReplyWriter = "This is a mock reply from a Mock Writer Actor"

  val unknownCommandViewerText = "Received unknown command \nSomeCommand\nTry one of the available queries from below: \n\nhttp://localhost:8080/view/GetQueries\nhttp://localhost:8080/view/GetCrimeTypes\nhttp://localhost:8080/view/GetDistricts\nhttp://localhost:8080/view/GetCrimesForDistrict"
}
