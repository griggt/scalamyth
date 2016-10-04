package mythtv
package connection
package myth

import java.util.regex.Pattern

trait MythProtocol extends MythProtocolLike {
  def supports(command: String): Boolean = commands contains command
  def supports(command: String, args: Any*): Boolean = {
    if (commands contains command) {
      val (check, _, _) = commands(command)
      check(args)
    }
    else false
  }
}

object MythProtocol extends MythProtocol {
  final val BACKEND_SEP: String = "[]:[]"
  final val SPLIT_PATTERN: String = Pattern.quote(BACKEND_SEP)
}

private[myth] trait MythProtocol75 extends MythProtocol with MythProtocolLike75
private[myth] trait MythProtocol77 extends MythProtocol with MythProtocolLike77
