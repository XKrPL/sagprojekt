package sagproject.mainapp

import sagproject.parser.Tokenizer

object MainApp {
  def main(args: Array[String]): Unit = {
    test1
  }
  
  def test1() {
    val tokenizer = new Tokenizer
    tokenizer.add("sss", 1)
    tokenizer.add("d", 2)
    try {
      tokenizer.tokenize("sss sssddd dsss")
    } catch {
      case e: Exception => e.printStackTrace();
      return
    }
    println(tokenizer.tokens)
  }
}