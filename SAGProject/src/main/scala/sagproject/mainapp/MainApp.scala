package sagproject.mainapp

import java.io.File
import java.util.Scanner
import java.util.concurrent.{TimeUnit, TimeoutException}

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import sagproject.communication.{Device, DeviceActor, SystemMessage}
import sagproject.parser.{SAGCommandParser, SAGFileParser}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.Breaks._

object MainApp extends LazyLogging {
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
        if (args.length != 0 && "test".equals(args(0))) {
          testScenario1(system, actorsList.get.map(actor => actor.actorName))
        } else {
          startUserPrompt(system, actorsList.get.map(actor => actor.actorName))
        }
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
                  logger.info("Otrzymano stan: " + deviceName + "=" + askResult)
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

  def testScenario1(system: ActorSystem, actorsList: List[String]) = {
    logger.info("Rozpoczeto wysyłanie wiadomosci.")
    for (i <- 1 until 1000) {
      (i % 7) match {
        case 0 => system.actorSelection("/user/" + "czujnik1") ! SystemMessage(SystemMessage.NONE, "ON")
        case 1 => system.actorSelection("/user/" + "czujnik1") ! SystemMessage(SystemMessage.NONE, "OFF")
        case 2 => system.actorSelection("/user/" + "drzwi") ! SystemMessage(SystemMessage.NONE, "CLOSED")
        case 3 => system.actorSelection("/user/" + "drzwi") ! SystemMessage(SystemMessage.NONE, "OPEN")
        case 4 => system.actorSelection("/user/" + "czujnikTemp") ! SystemMessage(SystemMessage.NONE, String.valueOf(i % 100))
        case 5 => system.actorSelection("/user/" + "czujnikSwiatla") ! SystemMessage(SystemMessage.NONE, String.valueOf(i % 100))
        case 6 => system.actorSelection("/user/" + "roleta") ! SystemMessage(SystemMessage.NONE, String.valueOf(i % 100))
      }
    }
    logger.info("Zakończono wysyłanie wiadomosci.")
  }
}