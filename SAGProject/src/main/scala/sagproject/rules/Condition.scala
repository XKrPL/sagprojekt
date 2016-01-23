package sagproject.rules

import com.typesafe.scalalogging.LazyLogging

/**
 * Represents one condition from configuration file.
 * Example: "czujnikSwiatla(>50) -> 100"
 *
 * @param actorName
 * @param relation
 */
case class Condition(actorName: String, relation: Relation) extends LazyLogging {
  override def toString = actorName + "(" + relation + ")"

  /**
   * Checks whether the condition is fulfilled.
   *
   * @param actorsStates actors' states that will be used for comparison in the condition
   * @return true if condition is fulfilled
   */
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