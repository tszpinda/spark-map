package tszpinda.twit

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory }
import com.vividsolutions.jts.io.WKTReader
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._

import tszpinda.twit.Utilities._
import twitter4j.Status
import twitter4j.GeoLocation

object Hello {

  def filterNoGeo(tweet:Status) : Option[Status] = {
    if(tweet.getGeoLocation() != null){
      return Some(tweet)
    }
    return None
  }
  
  def main(args: Array[String]) {
    println("hello")

    //mcInTiverton()
    setupTwitter()
    val ssc = new StreamingContext("local[2]", "AvgTweetLength", Seconds(10))
    setupLogging()
    val tweets = TwitterUtils.createStream(ssc, None)
    val location = tweets.flatMap(filterNoGeo)
    location.foreachRDD((rdd, time) => {
      val count = rdd.count()      
      if(count > 0){
        println("sending locations: " + count)
        rdd.foreachAsync(sendLocation)
      }
    })
    ssc.start()
    ssc.awaitTermination()
  }
  
  def sendLocation(item:Any) = {
    println("sending location: " + item)
    val status = item.asInstanceOf[Status];
    val lat = status.getGeoLocation().getLatitude()
    val lng = status.getGeoLocation().getLongitude()
    scala.io.Source.fromURL(s"http://localhost:3000/new?latitude=$lat&longitude=$lng").mkString
  }

  def mcInTiverton() = {
    val gf = new GeometryFactory()

    val lon = -3.468289375305176
    val lat = 50.91101599898483

    val tivertonMcDonalds = gf.createPoint(new Coordinate(lon, lat))
    val tivertonPolygonData = "POLYGON((-3.5163116455078125 50.92424609910129,-3.4452438354492188 50.92424609910129,-3.4452438354492188 50.889823771452626,-3.5163116455078125 50.889823771452626,-3.5163116455078125 50.92424609910129))"
    val tivertonArea = new WKTReader().read(tivertonPolygonData)
    assert(tivertonArea.contains(tivertonMcDonalds), "There is McDonalds in Tiverton")
  }
}
