package sagproject.parser
import java.util.regex.Pattern
import java.util.LinkedList
import java.lang.Boolean
import scala.collection.immutable.List
import scala.collection.mutable.MutableList
import java.util.regex.Matcher
import scala.util.control._

/**
 * class for tokenizing
 */
class Tokenizer {
  /** token infos in a list */
  private val tokenInfos = new MutableList[TokenInfo]
  /** found tokens */
  val tokens = new MutableList[Token]
  /**
   * adds new token to tokenizer
   */
  def add(regex: String, token: Integer) {
    tokenInfos += (new TokenInfo(Pattern.compile("^(" + regex + ")"), token))
  }

  /**
   * tokenizes the string and puts found tokens in list
   */
  def tokenize(str: String) {
    var s = new String(str).trim
    tokens.clear
    while (!s.equals("")) {
      var matched = false
      println("DBUG: currline: " + s)
      val loop = new Breaks;
      loop.breakable {
        for (info <- tokenInfos) {
          val m = info.regex.matcher(s)
          if (m.find) {
            matched = true
            println("DBUG: matched: " + info.token())
            val tok = m.group().trim()
            println("DBUG: tok:" + tok)
            tokens += new Token(info.token, tok)

            s = m.replaceFirst("").trim
            loop.break
          }
        }
      }
      if (!matched) {
        throw new ParserException("Unexpected character in input: " + s)
      }
    }

  }

  /**
   * token info (token definition)
   */
  class TokenInfo(regex: Pattern, token: Integer) {
    def token(): Integer = token
    def regex(): Pattern = regex
  }

  class ParserException(message: String) extends Exception
}