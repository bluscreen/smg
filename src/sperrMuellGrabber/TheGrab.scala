package sperrMuellGrabber

object TheGrab {
  def main(args: Array[String]) {
    val url = "http://web3.karlsruhe.de/service/abfall/akal/akal.php?von=A&bis=["

    val filepath = "C:\\TEMP\\muell.csv"

    // instanciate grabbor
    val grabbor = new Grabber()

    // bring it on... grab de shit!!
    val strassen = grabbor.getStreetNames(url)

    val termine = grabbor.getTermine(url, strassen)
    
    // now write de shit to file...
    grabbor.listToFile(termine, filepath)
  }
}