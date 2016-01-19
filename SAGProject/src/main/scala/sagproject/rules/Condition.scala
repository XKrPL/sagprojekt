package sagproject.rules

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

case class Condition(actorName: String, relation: Relation) {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  override def toString = actorName + "(" + relation + ")"

  def isFulfilled(actorsStates: Map[String, String]) = {
    val actorState = actorsStates.get(actorName)
    if (!actorState.isDefined) throw new RuntimeException("Actor state not found.")
    try {
      relation.relationOperator match {
        case Relation.EQUALS => actorState.get.equals(relation.value)
        case Relation.GREATER_THAN =>
          Integer.valueOf(actorState.get) > Integer.valueOf(relation.value)
        case Relation.LESS_THAN =>
          Integer.valueOf(actorState.get) < Integer.valueOf(relation.value)
      }
    } catch {
      case ex: NumberFormatException => {
        logger.warn("Non numerical state of actor " + actorName)
        false
      }
    }
  }
}