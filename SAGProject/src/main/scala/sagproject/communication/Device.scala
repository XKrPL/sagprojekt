package sagproject.communication

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import sagproject.rules.Rule

import scala.collection.mutable
import scala.collection.mutable.MutableList
import scala.concurrent.duration.Duration

/**
 * Helper class that is used for saving and loading configuration from file.
 * It is later transformed to DeviceActor.
 * @param actorName
 * @param currentState
 * @param rules
 */
case class Device(actorName: String,
                  var currentState: String,
                  rules: List[Rule],
                  var actorsToBeInformed: mutable.Set[String],
                  var otherActorsStates: Map[String, String]) {
  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))

  override def toString() = "Device[actorName= " + actorName + ",\n rules=\n " + rules + "]"

}