package sagproject.actors

class Condition(actorName: String, relation: Relation) {
  override def toString = actorName + "(" + relation + ")"
}