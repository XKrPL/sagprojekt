package sagproject.parser

class SAGTokenizer {
  // word
  val WORD = 0
  
  // brackets
  val OPEN_BRACKET = WORD + 1
  val CLOSED_BRACKET = OPEN_BRACKET + 1
  
  // bracers
  val OPEN_BRACE  = CLOSED_BRACKET + 1
  val CLOSED_BRACE = OPEN_BRACE + 1
  
  // integer
  val INTEGER = CLOSED_BRACE + 1
  
  // boolean values
  val ON = INTEGER + 1
  val OFF = ON + 1
  
  // operands
  val GREATER_THAN = OFF + 1
  val LESS_THAN = GREATER_THAN + 1
  val EQUAL = LESS_THAN + 1
  
  // implies
  val IMPLIES = EQUAL + 1
  
  // logical operators
  val OR = IMPLIES + 1
  val AND = OR + 1
  
  /** tokenizer */
  private val tokenizer = initTokenizer
  
  /**
   * tokenizes given string
   */
  def tokenize(str: String) {
    tokenizer.tokenize(str)
    println(tokenizer.tokens)
  }
  
  /**
   * to init tokenizer
   */
  private def initTokenizer : Tokenizer = {
    val newTokenizer = new Tokenizer
    newTokenizer.add("\\(", OPEN_BRACKET)
    newTokenizer.add("\\)", CLOSED_BRACKET)
    newTokenizer.add("\\{", OPEN_BRACE)
    newTokenizer.add("\\}", CLOSED_BRACE)
    newTokenizer.add("ON", ON)
    newTokenizer.add("OFF", OFF)
    newTokenizer.add(">", GREATER_THAN)
    newTokenizer.add("<", LESS_THAN)
    newTokenizer.add("=", EQUAL)
    newTokenizer.add("->", IMPLIES)
    newTokenizer.add("\\|\\|", OR)
    newTokenizer.add("&&", AND)
    newTokenizer.add("\\w+", WORD)
    newTokenizer.add("\\d+", INTEGER)
    return newTokenizer
  }
}