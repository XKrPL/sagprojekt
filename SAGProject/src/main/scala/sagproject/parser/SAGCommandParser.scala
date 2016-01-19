package sagproject.parser

import sagproject.communication.SystemMessage

object SAGCommandParser {

  /**
   * Parses one command from user.
   *
   * @param line input from user
   * @return pair deviceName -> SystemMessage
   */
  def parseUserCommand(line: String): Option[(String, SystemMessage)] = {
    val words = line.split("\\W+")
    if (words.length != 2) {
      return None
    }
    //ask -> device
    if(SystemMessage.ASK.equalsIgnoreCase(words(0))) {
      Some(words(1) -> SystemMessage(SystemMessage.ASK, null))
    } else {
      //device -> state
      Some(words(0) -> SystemMessage(SystemMessage.NONE, words(1)))
    }
  }
}
