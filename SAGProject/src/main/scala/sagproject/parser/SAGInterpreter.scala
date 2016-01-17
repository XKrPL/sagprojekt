package sagproject.parser

import java.io.File
import java.util.Scanner
import scala.collection.mutable.MutableList
import scala.collection.mutable.ListBuffer

class SAGInterpreter(file: File) {

  //scanner for sag files
  private val scanner = new Scanner(file)

  // sag tokenizer
  private val sagTokenizer = new SAGTokenizer

  // tokens from current line to process
  private val lineTokensToProcess = new ListBuffer[Token]

  //retrieves next token to process
  def getNextToken: Token = {
    while (lineTokensToProcess.isEmpty) {
      if (scanner.hasNextLine) {
        sagTokenizer.tokenize(scanner.nextLine)
        for (token <- sagTokenizer.tokens) {
          lineTokensToProcess += token
        }
      } else {
        scanner.close
        return null
      }
    }
    val head = lineTokensToProcess.head
    lineTokensToProcess -= head
    return head

  }

}