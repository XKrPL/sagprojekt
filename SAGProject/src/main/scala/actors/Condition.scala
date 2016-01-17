package actors

import sagproject.actors.Relation

class Condition(actorName: String, relation: Relation) {
  override def toString = actorName + "(" + relation + ")"
}