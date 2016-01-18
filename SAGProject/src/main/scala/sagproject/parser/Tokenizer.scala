package sagproject.parser

import java.util.regex.Pattern

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable.MutableList
import scala.util.control._

/**
 * class for tokenizing
 */
class Tokenizer {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
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
      logger.debug("DBUG: currline: " + s)
      val loop = new Breaks;
      loop.breakable {
        for (info <- tokenInfos) {
          val m = info.regex.matcher(s)
          if (m.find) {
            matched = true
            logger.debug("DBUG: matched: " + info.token())
            val tok = m.group().trim()
            logger.debug("DBUG: tok: " + tok)
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
  class TokenInfo(regexInput: Pattern, tokenInput: Integer) {
    def token(): Integer = tokenInput

    def regex(): Pattern = regexInput
  }

  class ParserException(message: String) extends Exception

}