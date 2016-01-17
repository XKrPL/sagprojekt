package actors

class Actor(actorName: String, rules: List[Rule]) {
  override def toString() = "Actor[actorName= " + actorName + ",\n rules=\n " + rules + "]" 
}