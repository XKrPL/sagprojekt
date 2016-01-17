package sagproject.mainapp

import java.io.File

import akka.actor.{ActorSystem, Props}
import sagproject.communication.{DeviceActor, SystemMessage}
import sagproject.parser.{SAGFileParser, SAGTokenizer, Tokenizer}

object MainApp {

  def main(args: Array[String]): Unit = {
    parseConfigFile
  }

  /**
   * Parses config file.
   */
  def parseConfigFile() {
    val sagParser = new SAGFileParser(new File("test.txt"))
    try {
      val actorsList = sagParser.parse
      actorsList.mkString("", "\n", "")
      println("parsing done")

      val system = ActorSystem("Main")
      actorsList.foreach(actor => system.actorOf((Props(
        DeviceActor(actor.actorName,
          actor.currentState,
          actor.rules,
          actor.actorsToBeInformed,
          actor.otherActorsStates))), name = actor.actorName))

      println("Sending message SystemMessage(\"NONE\", \"ON\") to czujnik1")
      system.actorSelection("/user/" + "czujnik1") ! SystemMessage("NONE", "ON")
      println("Sending message SystemMessage(\"NONE\", \"OPEN\") to drzwi")
      system.actorSelection("/user/" + "drzwi") ! SystemMessage("NONE", "OPEN")
      println("Sending message SystemMessage(\"NONE\", \"40\") to czujnikSwiatla")
      system.actorSelection("/user/" + "czujnikSwiatla") ! SystemMessage("NONE", "40")

    } catch {
      case e: Exception => println(e.getMessage)
        e.printStackTrace()
    }
  }


  //////////////////////////////////////
  // Test methods                     //
  //////////////////////////////////////
  def test1() {
    val tokenizer = new Tokenizer
    tokenizer.add("sss", 1)
    tokenizer.add("d", 2)
    try {
      tokenizer.tokenize("sss sssdddw dsss")
    } catch {
      case e: Exception =>
        e.printStackTrace();
        return
    }
    println(tokenizer.tokens)
  }

  def test2() {
    val sagParser = new SAGTokenizer

    sagParser.tokenize("czujnik1(ON) && drzwi(OPEN) -> ON")
  }
}