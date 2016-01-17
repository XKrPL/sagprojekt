package sagproject.parser

import java.io.File
import actors.{Condition, Actor, Rule}
import scala.collection.mutable.MutableList
import sagproject.actors.Relation

class SAGFileParser(file: File) {
  private val interpreter = new SAGInterpreter(file)
  private var currentToken: Token = null

  def parse: List[Actor] = {
    val list = new MutableList[Actor]
    var actor = parseActor
    while (actor != null) {
      list += actor
      actor = parseActor
    }
    return list.toList
  }

  private def parseActor: Actor = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    var actorName = parseOptionally(SAGTokenizer.WORD)
    if (actorName == null)
      return null
    parseObligatory(SAGTokenizer.OPEN_BRACE)
    val rules = parseRules
    parseObligatory(SAGTokenizer.CLOSED_BRACE)
    return new Actor(actorName, rules)
  }

  /**
   * if next token is not the one we expect throw an exception, else return its
   * sequence
   */
  private def parseObligatory(token: Integer): String = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    if (currentToken == null)
      currentToken = interpreter.getNextToken
    if (currentToken == null)
      throw new ParseException("unexpected end of stream, token " + token + " expected")
    println("token" + currentToken.sequence() )
    if (currentToken.token() == token) {
      val toRet = currentToken.sequence
      currentToken = null // consume token
      return toRet
    }
    println("token " + token + " expected, but "
      + currentToken.token + " found")
    throw new ParseException("token " + token + " expected, but "
      + currentToken.token + " found")
  }

  /**
   * if next token is not the one we expect return null, else return its
   * sequence
   */
  private def parseOptionally(token: Integer): String = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    if (currentToken == null)
      currentToken = interpreter.getNextToken
    if (currentToken == null)
      return null
    println("token" + currentToken.sequence() )
    if (currentToken.token() == token) {
      val toRet = currentToken.sequence
      currentToken = null // consume token
      return toRet
    }
    return null
  }

  private def parseRules: List[Rule] = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    val list = new MutableList[Rule]
    var rule = parseRule
    while (rule != null) {
      list += rule
      rule = parseRule
    }
    return list.toList
  }

  private def parseRule: Rule = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    val conditions = parseConditions
    if (conditions.isEmpty) 
      return null  
    val impliesState = parseImpliesState
    if (impliesState == null)
      return null
    return new Rule(conditions, impliesState)
  }

  private def parseConditions: List[Condition] = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    val list = new MutableList[Condition]
    var condition = parseCondition
    if (condition == null)
      return list.toList
    list += condition
    while (true) {
      if (parseOptionally(SAGTokenizer.AND) == null) {
        return list.toList
      }
      condition = parseCondition
      if (condition == null) {
        throw new ParseException("&& provided but no condition");
      }
      list += condition
    }
    return list.toList
  }

  private def parseCondition: Condition = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    var actorName = parseOptionally(SAGTokenizer.WORD)
    if (actorName == null)
      return null // no condition found
    parseObligatory(SAGTokenizer.OPEN_BRACKET)
    var relation = parseRelation
    parseObligatory(SAGTokenizer.CLOSED_BRACKET)
    return new Condition(actorName, relation)
  }

  private def parseRelation: Relation = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    var maybeEqualsValue = parseOptionally(SAGTokenizer.WORD)
    if (maybeEqualsValue != null) {
      return new Relation(Relation.EQUALS, maybeEqualsValue)
    }

    maybeEqualsValue = parseOptionally(SAGTokenizer.INTEGER)
    if (maybeEqualsValue != null) {
      return new Relation(Relation.EQUALS, maybeEqualsValue)
    }
    
    var maybeGreaterThanOperator = parseOptionally(SAGTokenizer.GREATER_THAN)
    if (maybeGreaterThanOperator != null) {
      var greaterThanValue = parseObligatory(SAGTokenizer.INTEGER)
      return new Relation(Relation.GREATER_THAN, greaterThanValue)
    }

    var maybeLessThanOperator = parseOptionally(SAGTokenizer.LESS_THAN)
    if (maybeLessThanOperator != null) {
      var lessThanValue = parseObligatory(SAGTokenizer.INTEGER)
      return new Relation(Relation.LESS_THAN, lessThanValue)
    }
    throw new ParseException("no relation found in a condition")
  }

  private def parseImpliesState: String = {
    println(Thread.currentThread.getStackTrace()(2).getMethodName)
    parseObligatory(SAGTokenizer.IMPLIES)
    val maybeInteger = parseOptionally(SAGTokenizer.INTEGER)
    if (maybeInteger != null)
      return maybeInteger
    return parseObligatory(SAGTokenizer.WORD)
  }

  class ParseException(message: String) extends Exception
}