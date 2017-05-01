package tszpinda.twit

import scala.io.Source
import spray.json.JsValue
import org.json4s.jackson.Json
import scalaz.stream.nio.file
import java.io.FileInputStream
import scala.io.Source

object Utilities {

  def setupLogging() = {
    import org.apache.log4j.{Level, Logger}   
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)   
  }
  
  def setupTwitter() = {
    implicit val formats = org.json4s.DefaultFormats

    val jsonIn = Source.fromFile("twitter.json").reader()
    val oauth = org.json4s.jackson.JsonMethods.parse(jsonIn).extract[Map[String, Any]]
    oauth.foreach {case (key, value) =>      
      System.setProperty("twitter4j.oauth." + key, value.toString())
    }
    
    oauth.foreach {x =>      
      System.setProperty("twitter4j.oauth." + x._1, x._2.toString())
    }
  }
  
}