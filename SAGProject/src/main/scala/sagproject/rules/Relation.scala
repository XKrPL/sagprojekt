package sagproject.rules

object Relation {
  val EQUALS = 0
  val GREATER_THAN = EQUALS + 1
  val LESS_THAN = GREATER_THAN + 1
}

case class Relation(relationOperator: Int, value: String) {
  override def toString: String = {
    val sign = relationOperator match
    {
    case Relation.EQUALS => "="
    case Relation.GREATER_THAN => ">"
    case Relation.LESS_THAN => "<"
    }
    return "" + sign + value
  }
}