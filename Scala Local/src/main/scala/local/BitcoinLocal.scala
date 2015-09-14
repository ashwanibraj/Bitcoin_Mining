package local

import java.security.MessageDigest

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

import scala.collection.mutable.ArrayBuffer
 
object BitcoinLocal extends App {
 
  val system = ActorSystem("BitcoinLocalSystem")

  var cmd_arg:String=args(0)  


  sealed trait BitcoinMessage    
  case class Work(inputString: String, noOfZero:Int, maxLen: Int) extends BitcoinMessage
  case class Result(strf: String, coin: String, working:Boolean) extends BitcoinMessage

  case class Wo1(sArr:ArrayBuffer[String])
  
  val worker = system.actorOf(Props[Worker], name = "Worker")  
  worker ! "Start"


  class Worker extends Actor {
    val master = context.actorFor("akka://BitcoinRemoteSystem@"+cmd_arg+":5150/user/Main")
    
    def sha256_hex(s: String): String = {
      MessageDigest.getInstance("SHA-256").digest(s.getBytes).map("%02X".format(_)).mkString
    }

    def sha256_str(str1: String, noOfZero:Int, max_len: Int) {
      var res:String = "-1"
      var comp:String = ""
      for (i <- 0 until noOfZero) {
        comp = comp + "0"
      }
      if (str1.length == max_len) {          
        var hexVal: String = sha256_hex(str1)        
        if (hexVal.substring(0,noOfZero) == comp) {
          sender ! Result(str1, hexVal, true)          
        }        
      }
      else {
        for ( j <- 33 to 126 ) {
          var str_temp: String = str1 + j.toChar          
          sha256_str(str_temp, noOfZero, max_len)
        }
        sender ! Result(res, res, false)
      }                  
    }    
 
    def receive = {
      case Work(inputString, noOfZero, maxLen) ⇒
        sha256_str(inputString, noOfZero, maxLen)                
      case "Start" ⇒        
        master ! "Start"                 
    }
  }
 
}