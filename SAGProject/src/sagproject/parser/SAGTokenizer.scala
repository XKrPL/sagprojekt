package sagproject.parser

import scala.collection.mutable.MutableList

object SAGTokenizer {
  // word
  val WORD = 0

  // brackets
  val OPEN_BRACKET = WORD + 1
  val CLOSED_BRACKET = OPEN_BRACKET + 1

  // bracers
  val OPEN_BRACE = CLOSED_BRACKET + 1
  val CLOSED_BRACE = OPEN_BRACE + 1

  // integer
  val INTEGER = CLOSED_BRACE + 1

  //  // boolean values
  //  val ON = INTEGER + 1
  //  val OFF = ON + 1

  // operands
  val GREATER_THAN = INTEGER + 1
  val LESS_THAN = GREATER_THAN + 1
  val EQUAL = LESS_THAN + 1

  // implies
  val IMPLIES = EQUAL + 1

  // logical operators
  val OR = IMPLIES + 1
  val AND = OR + 1

}

class SAGTokenizer {

  /** tokenizer */
  private val tokenizer = initTokenizer

  /**
   * tokenizes given string
   */
  def tokenize(str: String) {
    tokenizer.tokenize(str)
  }

  /**
   * returns found SAG tokens
   */
  def tokens: MutableList[Token] = tokenizer.tokens

  /**
   * to init tokenizer
   */
  private def initTokenizer: Tokenizer = {
    val newTokenizer = new Tokenizer
    newTokenizer.add("\\(", SAGTokenizer.OPEN_BRACKET)
    newTokenizer.add("\\)", SAGTokenizer.CLOSED_BRACKET)
    newTokenizer.add("\\{", SAGTokenizer.OPEN_BRACE)
    newTokenizer.add("\\}", SAGTokenizer.CLOSED_BRACE)
    newTokenizer.add(">", SAGTokenizer.GREATER_THAN)
    newTokenizer.add("<", SAGTokenizer.LESS_THAN)
    newTokenizer.add("=", SAGTokenizer.EQUAL)
    newTokenizer.add("->", SAGTokenizer.IMPLIES)
    newTokenizer.add("\\|\\|", SAGTokenizer.OR)
    newTokenizer.add("&&", SAGTokenizer.AND)
    newTokenizer.add("\\w+", SAGTokenizer.WORD)
    newTokenizer.add("\\d+", SAGTokenizer.INTEGER)
    return newTokenizer
  }
}