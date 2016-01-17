package sagproject.rules

case class Condition(actorName: String, relation: Relation) {
  override def toString = actorName + "(" + relation + ")"

  def isFulFilled(actorsStates: Map[String, String]) = {
    val actorState = actorsStates.get(actorName)
    if (!actorState.isDefined)   throw new RuntimeException("Actor state not found.")
    relation.relationOperator match {
      case Relation.EQUALS => actorState.get.equals(relation.value)
      case Relation.GREATER_THAN =>
        Integer.valueOf(actorState.get) > Integer.valueOf(relation.value)
      case Relation.LESS_THAN =>
        Integer.valueOf(actorState.get) < Integer.valueOf(relation.value)
    }
  }
}