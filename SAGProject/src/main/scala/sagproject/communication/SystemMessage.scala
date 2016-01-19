package sagproject.communication

object SystemMessage {
  /**
   * Message simulating hardware.
   * This sort of message is used to set current state of the actor.
   */
  val NONE = "NONE"
  /**
   * This message is used to ask actor for its current state.
   */
  val ASK = "ASK"
}

/**
 * Class that is transporting informations between actor.
 *
 * @param source actor that generated that message
 * @param message transported value
 */
case class SystemMessage(source: String, message: String) {

}
