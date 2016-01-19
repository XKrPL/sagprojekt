package sagproject.mainapp

import java.io.File
import java.util.Scanner
import java.util.concurrent.{TimeUnit, TimeoutException}

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import sagproject.communication.{Device, DeviceActor, SystemMessage}
import sagproject.parser.{SAGCommandParser, SAGFileParser}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.Breaks._

object MainApp {
  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))

  def main(args: Array[String]): Unit = {
    val actorsList = parseConfigFile
    val system = actorsList match {
      case Some(deviceList) => Some(createSystem(deviceList))
      case _ => None
    }
    system match {
      case Some(system) => {
        //      println("Sending message SystemMessage(\"NONE\", \"ON\") to czujnik1")
        //      system.actorSelection("/user/" + "czujnik1") ! SystemMessage("NONE", "ON")
        //      println("Sending message SystemMessage(\"NONE\", \"OPEN\") to drzwi")
        //      system.actorSelection("/user/" + "drzwi") ! SystemMessage("NONE", "OPEN")
        //      println("Sending message SystemMessage(\"NONE\", \"40\") to czujnikSwiatla")
        //      system.actorSelection("/user/" + "czujnikSwiatla") ! SystemMessage("NONE", "40")
        startUserPrompt(system, actorsList.get.map(actor => actor.actorName))
      }
      case _ => None
    }
  }

  /**
   * Creates akka system with actors.
   *
   * @param deviceList list of actors
   * @return return created system that can be later referenced
   */
  def createSystem(deviceList: List[Device]) = {
    val system = ActorSystem("Main")
    deviceList.foreach(device => system.actorOf((Props(
      DeviceActor(device.actorName,
        device.currentState,
        device.rules,
        device.actorsToBeInformed,
        device.otherActorsStates))), name = device.actorName))
    system
  }

  /**
   * Parses config file.
   */
  def parseConfigFile = {
    val sagParser = new SAGFileParser(new File("test.txt"))
    try {
      val actorsList = sagParser.parse
      actorsList.mkString("", "\n", "")
      logger.info("Parsing done.")
      logger.info(actorsList.mkString("", "\n", ""))
      Some(actorsList)
    } catch {
      case e: Exception => {
        println(e.getMessage)
        e.printStackTrace()
        None
      }
    }
  }

  /**
   * Starts loop for interaction with user.
   *
   * @param system akka system
   * @param actorsList names of the configured actors
   */
  def startUserPrompt(system: ActorSystem, actorsList: List[String]) = {
    val reader = new Scanner(System.in)
    breakable {
      while (true) {
        println("Podawaj polecenia. Wpisz \"help\" po pomoc.")
        val line = reader.nextLine()

        if ("help".equals(line)) {
          printHelp
        } else if ("exit".equals(line)) {
          break
        } else if ("list".equals(line)) {
          println(actorsList.mkString(","))
        } else {
          val userCommand = SAGCommandParser.parseUserCommand(line)
          userCommand match {
            //asking for device value with Future usage
            case Some((deviceName, SystemMessage(SystemMessage.ASK, _))) => {
              if (actorsList.contains(deviceName)) {
                try {
                  val actorRef = Await.result(system.actorSelection("/user/" + deviceName).resolveOne(), timeout.duration)
                  val askFuture = actorRef ? SystemMessage(SystemMessage.ASK, null)
                  val askResult = Await.result(askFuture, timeout.duration).asInstanceOf[String]
                  println("Otrzymano stan: " + deviceName + "=" + askResult)
                } catch {
                  case ex: TimeoutException => {
                    //ignore
                  }
                }
              } else {
                println("Podane urzadzenie " + deviceName + " nie istnieje.")
              }
            }
            //simple sending message to device
            case Some((deviceName, command)) => {
              if (actorsList.contains(deviceName)) {
                system.actorSelection("/user/" + deviceName) ! command
              } else {
                println("Podane urzadzenie " + deviceName + " nie istnieje.")
              }
            }
            case None => println("Niepoprawny format komendy.")
          }
        }
      }
    }
  }

  def printHelp = {
    println("nazwaUrzadzenia wartosc - ustawienie wartosci na urządzeniu")
    println("ask nazwaUrzadzenia - zapytanie o wartossc")
    println("list - wyswietla liste nazw urzadzen")
    println("help - pomoc")
    println("exit - wyjscie")

  }
}