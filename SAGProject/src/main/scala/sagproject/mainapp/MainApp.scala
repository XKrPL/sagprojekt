package sagproject.mainapp

import java.io.File
import java.util.Scanner

import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import sagproject.communication.{Device, DeviceActor}
import sagproject.parser.{SAGCommandParser, SAGFileParser}

import scala.util.control.Breaks._

object MainApp {
  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

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
        startUserPrompt(system)
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
   */
  def startUserPrompt(system: ActorSystem) = {
    val reader = new Scanner(System.in)
    breakable {
      while (true) {
        println("Wpisz wiadomosci w formacie \"nazwaUrzadzenia -> wartosc\"")
        val line = reader.nextLine()

        if ("help".equals(line)) {
          println("Podaj komendę lub help (pomoc).")
          println("exit - wyjscie")
        } else if ("exit".equals(line)) {
          break
        } else {
          val userCommand = SAGCommandParser.parseUserCommand(line)
          userCommand match {
            case Some((deviceName, command)) => system.actorSelection("/user/" + deviceName) ! command
            case None => println("Niepoprawny format komendy.")
          }
        }
      }
    }
  }
}