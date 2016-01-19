package sagproject.rules

case class Rule(conditions: List[Condition], impliesState: String) {
  override def toString = conditions.mkString("", " && ", "") + " -> " + impliesState

  /**
   * Checks if all conditions are fulfilled.
   * @param actorsStates
   * @return
   */
  def isFulFilled(actorsStates: Map[String, String]) = {
    conditions.foldLeft(true)((result, condition) => result && condition.isFulfilled(actorsStates))
  }
}