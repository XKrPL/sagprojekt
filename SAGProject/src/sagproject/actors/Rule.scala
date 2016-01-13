package sagproject.actors

class Rule(conditions: List[Condition], impliesState: String) {
  override def toString = conditions.mkString("", " && ", "") + " -> " + impliesState
}