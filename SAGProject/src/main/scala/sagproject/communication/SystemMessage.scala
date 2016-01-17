package sagproject.communication

object SystemMessage {
  val NONE = "NONE"
}

/**
 * Class that is transporting informations between actor.
 *
 * @param source actor that generated that message
 * @param message transported value
 */
case class SystemMessage(source: String, message: String) {

}
