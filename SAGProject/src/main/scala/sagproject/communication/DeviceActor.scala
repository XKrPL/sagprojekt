package sagproject.communication

import akka.actor.Actor
import sagproject.rules.Rule

import scala.collection.mutable

/**
 * Class that is ised inside Akka framework.
 * Each actor is representing one real device.
 *
 * @param actorName name of the device
 * @param currentState current state of the device
 * @param rules rules representing interactions between devices
 * @param actorsToBeInformed actors that has to be informed about inner state change each time the
 *                           device changes its state
 * @param otherActorsStates other actors states that must be tracked by this actor
 */
case class DeviceActor(actorName: String,
                       var currentState: String,
                       rules: List[Rule],
                       var actorsToBeInformed: mutable.Set[String],
                       var otherActorsStates: Map[String, String]) extends Actor {
  override def toString() = "DeviceActor[actorName=" + actorName +
    ", currentState=" + currentState + ", rules= " + rules + "]"

  override def receive = {
    //message received from system or simulated by user
    case SystemMessage(SystemMessage.NONE, message) => {
      println("Received " + message + " from system inside " + actorName)
      changeInnerState(message)
      currentState = message
      //information send to all actors that depends on this actor
      actorsToBeInformed.foreach(actorToBeInformed => context.actorSelection("/user/" + actorToBeInformed)
        ! SystemMessage(actorName, currentState))
    }
    //message requesting for state of ths device
    case SystemMessage(SystemMessage.ASK, _) => {
      sender ! currentState
    }
    //message received from other actor
    case SystemMessage(source, message) => {
      println("Received " + message + " from " + source + " inside " + actorName)
      otherActorsStates += (source -> message)
      //change state that is arised by first fulfilled condition
      for (rule <- rules) {
        if (rule.isFulFilled(otherActorsStates)) {
          self ! SystemMessage(SystemMessage.NONE, rule.impliesState)
        }
      }
    }
  }

  def changeInnerState(newState: String) = {
    println("Changing inner state of " + actorName + " to: " + newState)
    currentState = newState
  }
}