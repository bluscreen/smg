package sperrMuellGrabber

import scala.collection.mutable.ListBuffer
import org.htmlcleaner.HtmlCleaner
import java.net.URL
import java.io.PrintWriter
import java.io.File
import java.util.ArrayList
import java.io._
import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods.HttpPost
import org.apache.http.util.EntityUtils
import org.apache.http.impl.client.DefaultHttpClient
import java.util.ArrayList
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.util.EntityUtils

class Grabber {
  def getStreetNames(url: String): List[NameValuePair] = {
    var streets = new ListBuffer[NameValuePair]
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    val rootNode = cleaner.clean(new URL(url))
    val elements = rootNode.getElementsByName("option", true)
    println("fetching street names: " + url)
    for (elem <- elements) {
      val nvp = new BasicNameValuePair(elem.getText.toString, elem.getAttributeByName("value"))
      println("Strasse:" + nvp.getName + " value: " + nvp.getValue)
      streets += nvp
    }
    println("done fetching streets")
    return streets.toList
  }

  def getTermine(url: String, strassen: List[NameValuePair]): List[String] = {
    var termine = new ListBuffer[String]
    println("fetching dates...")
    for (strasse <- strassen) {
      val bufAppend = strasse.getName + ";" + getTermin(url, strasse.getValue) + "\n"
      println(bufAppend)
      termine += bufAppend
    }
    println("done fetching dates")
    return termine.toList
  }

  def getTermin(strUrl: String, optId: String): String = {
    var termin = ""
    val post = new HttpPost(strUrl)
    val client = new DefaultHttpClient
    val nameValuePairs = new ArrayList[NameValuePair](1)
    nameValuePairs.add(new BasicNameValuePair("strasse", optId));
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

    // send the post request
    val response = client.execute(post)
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    val realresponse = EntityUtils.toString(response.getEntity, "UTF-8")
    //    println(realresponse)
    val rootNode = cleaner.clean(realresponse)
    //    println(rootNode)
    val elements = rootNode.getElementsByName("td", true)
    for (elem <- elements) {
      val text = elem.getText.toString
      if (text.equals("Sperrmüllabholung")) {
        /**
         * if i found sth i will jump to the TR and
         * check out its TD at index 2 which hopefully contains the dates..
         */
        val tr = elem.getParent
        termin = tr.getChildren.get(2).getText.toString
        if (termin.startsWith("Anmeldung")) {
          termin = "Nix is"
        }
        termin.replaceAll("und", ";")
        termin.trim
      }
    }
    return termin
  }

  def listToFile(theList: List[String], filePath: String) = {
    val writer = new PrintWriter(new File(filePath))
    println("I wanna write to: " + filePath)
    for (elem <- theList) {
      println("Trying to Write line: " + elem)
      writer.write(elem)
    }
    writer.close()
  }

}