package sagproject.parser

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import sagproject.communication.Device
import sagproject.rules.{Condition, Relation, Rule}

import scala.collection.mutable
import scala.collection.mutable.MutableList

class SAGFileParser(file: File) extends LazyLogging {
  private val interpreter = new SAGInterpreter(file)
  private var currentToken: Token = null

  def parse: List[Device] = {
    var actorsMap = Map[String, Device]()
    //list of actors that have to be informed by key
    var actorsToBeInformed = Map[String, mutable.Set[String]]()
    //list of actors that state has to be kept by key
    var actorsToHoldState = Map[String,  mutable.Set[String]]()

    var actor = parseActor
    //create actor
    while (actor != null) {
      //TODO extract to method
      actor.rules.foreach(rule => rule.conditions.foreach(condition => {
        //update that newly created actor has to be informed by the actors in the condition
        if (!actorsToBeInformed.get(condition.actorName).isDefined) {
          actorsToBeInformed += (condition.actorName -> new mutable.HashSet[String])
        }
        actorsToBeInformed.get(condition.actorName).get += actor.actorName
        //update that newly created actor has to keep the satet of all actors in the conditions
        if (!actorsToHoldState.get(actor.actorName).isDefined) {
          actorsToHoldState += (actor.actorName -> new mutable.HashSet[String])
        }
        actorsToHoldState.get(actor.actorName).get += condition.actorName
      }))

      actorsMap += (actor.actorName -> actor)
      actor = parseActor
    }

    //update actors inner lists
    actorsToBeInformed.foreach {
      case (actorName, actorsToBeInformedList) => {
        actorsMap.get(actorName).get.actorsToBeInformed = actorsToBeInformedList;
      }
    }
    actorsToHoldState.foreach {
      case (actorName, actorsToHoldStateList) => {
        //here actors inner states are now filled, because they will be initialized inside actor
        actorsMap.get(actorName).get.otherActorsStates = actorsToHoldStateList.map(actor => (actor -> null)).toMap;
      }
    }
    return actorsMap.values.toList
  }

  private def parseActor: Device = {
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
    val actorName = parseOptionally(SAGTokenizer.WORD)
    if (actorName == null)
      return null
    parseObligatory(SAGTokenizer.COLON)
    var actorState = parseOptionally(SAGTokenizer.INTEGER)
    if (actorState == null) {
      actorState = parseObligatory(SAGTokenizer.WORD)
    }
    parseObligatory(SAGTokenizer.OPEN_BRACE)
    val rules = parseRules
    parseObligatory(SAGTokenizer.CLOSED_BRACE)
    return new Device(actorName, actorState, rules, new mutable.HashSet[String](), Map[String, String]())
  }

  /**
   * if next token is not the one we expect throw an exception, else return its
   * sequence
   */
  private def parseObligatory(token: Integer): String = {
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
    if (currentToken == null)
      currentToken = interpreter.getNextToken
    if (currentToken == null)
      throw new ParseException("unexpected end of stream, token " + token + " expected")
    logger.debug("token " + currentToken.sequence())
    if (currentToken.token() == token) {
      val toRet = currentToken.sequence
      currentToken = null // consume token
      return toRet
    }
    logger.debug("token " + token + " expected, but "
      + currentToken.token + " found")
    throw new ParseException("token " + token + " expected, but "
      + currentToken.token + " found")
  }

  /**
   * if next token is not the one we expect return null, else return its
   * sequence
   */
  private def parseOptionally(token: Integer): String = {
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
    if (currentToken == null)
      currentToken = interpreter.getNextToken
    if (currentToken == null)
      return null
    logger.debug("token " + currentToken.sequence())
    if (currentToken.token() == token) {
      val toRet = currentToken.sequence
      currentToken = null // consume token
      return toRet
    }
    return null
  }

  private def parseRules: List[Rule] = {
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
    val list = new MutableList[Rule]
    var rule = parseRule
    while (rule != null) {
      list += rule
      rule = parseRule
    }
    return list.toList
  }

  private def parseRule: Rule = {
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
    val conditions = parseConditions
    if (conditions.isEmpty)
      return null
    val impliesState = parseImpliesState
    if (impliesState == null)
      return null
    return new Rule(conditions, impliesState)
  }

  private def parseConditions: List[Condition] = {
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
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
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
    var actorName = parseOptionally(SAGTokenizer.WORD)
    if (actorName == null)
      return null // no condition found
    parseObligatory(SAGTokenizer.OPEN_BRACKET)
    var relation = parseRelation
    parseObligatory(SAGTokenizer.CLOSED_BRACKET)
    return new Condition(actorName, relation)
  }

  private def parseRelation: Relation = {
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
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
    logger.debug(Thread.currentThread.getStackTrace()(2).getMethodName)
    parseObligatory(SAGTokenizer.IMPLIES)
    val maybeInteger = parseOptionally(SAGTokenizer.INTEGER)
    if (maybeInteger != null)
      return maybeInteger
    return parseObligatory(SAGTokenizer.WORD)
  }

  class ParseException(message: String) extends Exception

}