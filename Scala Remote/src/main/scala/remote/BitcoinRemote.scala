package remote

import java.security.MessageDigest

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.concurrent._

import scala.collection.mutable.ArrayBuffer
 
object BitcoinRemote extends App {
 
  val noOfZero:Int = args(0).toInt

  calculate(nrOfWorkers = 20, noOfZero = noOfZero)


  sealed trait BitcoinMessage
  case object Calculate extends BitcoinMessage
  case class Work(inputString: String, noOfZero:Int, maxLen: Int) extends BitcoinMessage
  case class Result(strf: String, coin: String, working:Boolean) extends BitcoinMessage
  case class TimeApproximation(duration: Duration)
 
  class Worker extends Actor {
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
    }
  }
 
  class Master(nrOfWorkers: Int, noOfZero:Int, listener: ActorRef)
    extends Actor {
     
    var nrOfResults: Int = _
    var nrOfReturns: Int = 0    
    var enI:Int = 10
    var inputString: String = "abraj" 
    val start: Long = System.currentTimeMillis / 1000
 
    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
 
    def receive = {
      case msg:String ⇒
        for (i ← enI until enI+3) sender ! (workerRouter ! Work(inputString, noOfZero, inputString.length + i))
        enI += 3        
      case Calculate ⇒
        for (i ← 0 until 10) workerRouter ! Work(inputString, noOfZero, inputString.length + i)      
      case Result(strf, coin, working) ⇒
        if (working) {
          nrOfResults += 1
          println("\n%d.\t%s :\t%s\t%s secs".format(nrOfResults, strf, coin, (System.currentTimeMillis/1000 - start)))          
        }
        else {
          nrOfReturns += 1
          if (nrOfReturns > 1000000) {
            println("\n\tTotal bitcoins found:\t" + nrOfResults)
            listener ! TimeApproximation(duration = (System.currentTimeMillis - start).millis)          
            context.stop(self)
          }
        }
    }
 
  }
 
  class Listener extends Actor {
    def receive = {      
      case TimeApproximation(duration) ⇒
        println("\n\tCalculation time: \t%s"
          .format(duration))
        context.system.shutdown()
    }
  }
 
 
  def calculate(nrOfWorkers: Int, noOfZero:Int) {
    // Create an Akka system
    val system = ActorSystem("BitcoinRemoteSystem")
 
    val listener = system.actorOf(Props[Listener], name = "listener")
 
    // create the master
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, noOfZero, listener)),
      name = "master")
 
    // start the calculation
    master ! Calculate
  }
}