package sagproject.communication

import java.util.concurrent.{TimeUnit, TimeoutException}

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import sagproject.rules.Rule

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

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
  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))
  /**
   * Variable describing if initializing of this device was already performed.
   */
  private var wasInitialized = false

  override def toString() = "DeviceActor[actorName=" + actorName +
    ", currentState=" + currentState + ", rules= " + rules + "]"

  override def receive = {
    //message received from system (hardware) or simulated by user
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
      if (!wasInitialized) {
        initialize
      }
      if (wasInitialized) {
        println("Received " + message + " from " + source + " inside " + actorName)
        otherActorsStates += (source -> message)
        //change state that is arised by first fulfilled condition
        var stateChanged = false
        for (rule <- rules) {
          if (rule.isFulFilled(otherActorsStates) && !stateChanged) {
            self ! SystemMessage(SystemMessage.NONE, rule.impliesState)
            stateChanged = true
          }
        }
      }
    }
  }

  /**
   * Changes current state of the device without any condition.
   *
   * @param newState stete to be set.
   */
  def changeInnerState(newState: String) = {
    println("Changing inner state of " + actorName + " to: " + newState)
    currentState = newState
  }

  /**
   * Initializes all necessary things for so that the device can work correctly.
   */
  def initialize = {
    try {
      otherActorsStates = otherActorsStates.map {
        case (deviceName, deviceState) => {
          //ask device for its state
          val actorRef = Await.result(context.actorSelection("/user/" + deviceName).resolveOne(), timeout.duration)
          val askFuture = actorRef ? SystemMessage(SystemMessage.ASK, null)
          val askResult = Await.result(askFuture, timeout.duration).asInstanceOf[String]
          (deviceName -> askResult)
        }
      }
      println("Device " + actorName + " was initialized")
      wasInitialized = true
    } catch {
      case ex: TimeoutException => {
        wasInitialized = false
        println("Unable to initialize device " + actorName)
      }
    }

  }
}