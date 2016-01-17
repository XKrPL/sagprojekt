package sagproject.parser

/**
 * real token (token value as string and type)
 */
class Token(tokenInput: Integer, sequenceInput: String) {
  def token(): Integer = tokenInput
  def sequence(): String = sequenceInput
  override def toString() = new String("Token[token: " + token + ", sequence: " + sequence + "]")
}
