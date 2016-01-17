package sagproject.parser

/**
 * real token (token value as string and type)
 */
class Token(token: Integer, sequence: String) {
  def token(): Integer = token
  def sequence(): String = sequence
  override def toString() = new String("Token[token: " + token + ", sequence: " + sequence + "]")
}
