package sagproject.communication

import sagproject.rules.Rule

import scala.collection.mutable

/**
 * Helper class that is used for saving and loading configuration from file.
 * It is later transformed to DeviceActor.
 *
 * @param actorName name of the device
 * @param currentState current state of the device
 * @param rules
 */
case class Device(actorName: String,
                  var currentState: String,
                  rules: List[Rule],
                  var actorsToBeInformed: mutable.Set[String],
                  var otherActorsStates: Map[String, String]) {
  override def toString() = "DeviceActor[actorName=" + actorName +
    ", currentState=" + currentState + ", rules= " + rules + "]"

}