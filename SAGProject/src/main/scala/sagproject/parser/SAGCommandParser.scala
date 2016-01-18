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
    Some((words(0), SystemMessage("NONE", words(1))))
  }
}
