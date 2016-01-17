package sagproject.mainapp

import sagproject.parser.Tokenizer
import sagproject.parser.SAGFileParser
import sagproject.parser.SAGTokenizer
import java.io.File

object MainApp {
  def main(args: Array[String]): Unit = {
    test3
  }

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

  def test3() {
    val sagParser = new SAGFileParser(new File("test.txt"))
    try {
      println(sagParser.parse.mkString("","\n",""))
    } catch {
      case e: Exception=> println(e.getMessage)
      e.printStackTrace()
    }
    println("parsing done")
  }
}