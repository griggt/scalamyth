// SPDX-License-Identifier: LGPL-2.1-only
/*
 * BackendConnection.scala
 *
 * Copyright (c) 2016-2018 Tom Grigg <tom@grigg.io>
 */
package mythtv
package connection
package myth

import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.{ SelectionKey, Selector, SocketChannel }

import scala.util.{ Try, Success, Failure }

import com.typesafe.scalalogging.LazyLogging

import MythProtocol.MythProtocolFailure._

private trait BackendCommandStream {
  final val HeaderSizeBytes = 8
}

private class BackendCommandReader(channel: SocketChannel, conn: SocketConnection)
  extends AbstractSocketReader[String](channel, conn)
     with BackendCommandStream
     with LazyLogging {
  private[this] var buffer = ByteBuffer.allocate(1024)  // will reallocate as needed

  // TODO handle AsynchronousCloseException and ClosedByInterruptException? (see InterruptibleChannel)
  //    hierarchy: > IOException > ClosedChannelException > AsynchronousClosedException > ClosedByInterruptException
  //
  private def readStringOfLength(length: Int): String = {
    if (length > buffer.capacity) buffer = ByteBuffer.allocate(length)
    buffer.clear().limit(length)

    val selector = Selector.open
    try {
      val key = channel.register(selector, SelectionKey.OP_READ)
      var n: Int = 0
      do {
        val ready = selector.select(conn.timeout * 1000)
        if (Thread.interrupted()) throw new InterruptedException
        if (ready == 0) throw new SocketTimeoutException(s"read timed out after ${conn.timeout} seconds")

        if (key.isReadable) {
          n = channel.read(buffer)
          selector.selectedKeys.clear()
          //println("Read " + n + " bytes")
        }
      } while (n > 0 && buffer.hasRemaining)

      if (n < 0) throw new EndOfStreamException

    } finally {
      selector.close()
    }

    utf8.decode({ buffer.flip(); buffer }).toString
  }

  def read(): String = {
    //println("Waiting for size header")
    val size = readStringOfLength(HeaderSizeBytes).trim.toInt
    //println("Waiting for response of length " + size)
    val response = readStringOfLength(size)
    logger.debug(s"Received response: $response")
    //println("Received response.")
    response
  }
}

private class BackendCommandWriter(channel: SocketChannel, conn: SocketConnection)
  extends AbstractSocketWriter[String](channel, conn)
     with BackendCommandStream
     with LazyLogging {
  private final val HeaderFormat= "%-" + HeaderSizeBytes + "d"

  private[this] val buffers = new Array[ByteBuffer](2)

  def write(command: String): Unit = {
    val message = utf8 encode command
    val header = utf8 encode (HeaderFormat format message.limit())

    buffers(0) = header
    buffers(1) = message
    logger.debug(s"Sending command $command")

    while (message.hasRemaining) {
      val n = channel.write(buffers)
      if (n == 0) waitForWriteableSocket()
    }
  }
}

final case class WrongMythProtocolException(requiredVersion: Int)
    extends RuntimeException("wrong Myth protocol version; need version " + requiredVersion)

final case class UnsupportedMythProtocolException(requiredVersion: Int)
    extends RuntimeException(s"unsupported Myth protocol version $requiredVersion requested") {
  def this(ex: WrongMythProtocolException) = this(ex.requiredVersion)
}

final case class UnsupportedBackendCommandException(command: String, protocolVersion: Int)
    extends UnsupportedOperationException(
  s"unsupported backend command $command in protocol version $protocolVersion")

trait BackendConnection extends SocketConnection with MythProtocol

private abstract class AbstractBackendConnection(host: String, port: Int, timeout: Int)
  extends AbstractSocketConnection[String](host, port, timeout)
     with BackendConnection {

  protected def finishConnect(): Unit = {
    checkVersion()
  }

  protected def gracefulDisconnect(): Unit = postCommandRaw("DONE")

  protected def openReader(channel: SocketChannel): SocketReader[String] =
    new BackendCommandReader(channel, this)

  protected def openWriter(channel: SocketChannel): SocketWriter[String] =
    new BackendCommandWriter(channel, this)

  protected def sendCommandRaw(command: String): Try[BackendResponse] = {
    Try {
      writer.write(command)
      BackendResponse(reader.read())
    }
  }

  protected def postCommandRaw(command: String): Unit = {
    // write the message
    writer.write(command)

    // don't attempt to read any response; we aren't expecting one and socket may be closed
  }

  def sendCommand(command: String, args: Any*): MythProtocolResult[_] = {
    if (!isConnected) throw new ClosedConnectionException
    if (commands contains command) {
      val (serialize, handle) = commands(command)
      val cmdstring = serialize(command, args)
      val request = BackendRequest(command, args, cmdstring)
      sendCommandRaw(cmdstring) match {
        case Success(response) => handle(request, response)
        case Failure(ex) => Left(MythProtocolFailureThrowable(ex))
      }
    }
    else throw UnsupportedBackendCommandException(command, ProtocolVersion)
  }

  private[myth] def postCommand(command: String, args: Any*): Unit = {
    if (!isConnected) throw new ClosedConnectionException
    if (commands contains command) {
      val (serialize, _) = commands(command)
      val cmdstring = serialize(command, args)
      postCommandRaw(cmdstring)
    }
    else throw UnsupportedBackendCommandException(command, ProtocolVersion)
  }

  def checkVersion(): Boolean = checkVersion(ProtocolVersion, ProtocolToken)

  def checkVersion(version: Int, token: String): Boolean = {
    val msg = for {
      response <- sendCommandRaw(s"MYTH_PROTO_VERSION $version $token")
      split <- Try(response.split)
    } yield (split(0), split(1))

    val (status, requiredVersion) = msg.get
    if (status == "ACCEPT") true
    else {
      disconnect(false)
      throw WrongMythProtocolException(requiredVersion.toInt)
    }
  }
}

private class BackendConnection75(host: String, port: Int, timeout: Int)
    extends AbstractBackendConnection(host, port, timeout) with MythProtocol75

private class BackendConnection77(host: String, port: Int, timeout: Int)
    extends AbstractBackendConnection(host, port, timeout) with MythProtocol77

private class BackendConnection88(host: String, port: Int, timeout: Int)
    extends AbstractBackendConnection(host, port, timeout) with MythProtocol88

private class BackendConnection91(host: String, port: Int, timeout: Int)
    extends AbstractBackendConnection(host, port, timeout) with MythProtocol91

private sealed trait BackendConnectionFactory {
  def apply(host: String, port: Int, timeout: Int): BackendConnection
}

private object BackendConnection75 extends BackendConnectionFactory {
  def apply(host: String, port: Int, timeout: Int) = new BackendConnection75(host, port, timeout)
}

private object BackendConnection77 extends BackendConnectionFactory {
  def apply(host: String, port: Int, timeout: Int) = new BackendConnection77(host, port, timeout)
}

private object BackendConnection88 extends BackendConnectionFactory {
  def apply(host: String, port: Int, timeout: Int) = new BackendConnection88(host, port, timeout)
}

private object BackendConnection91 extends BackendConnectionFactory {
  def apply(host: String, port: Int, timeout: Int) = new BackendConnection91(host, port, timeout)
}

object BackendConnection {
  final val DefaultPort = 6543
  final val DefaultTimeout = 10

  private val supportedVersions = Map[Int, BackendConnectionFactory](
    75 -> BackendConnection75,
    77 -> BackendConnection77,
    88 -> BackendConnection88,
    91 -> BackendConnection91,
  )

  private[myth] val DefaultVersion = 91

  def apply(host: String, port: Int = DefaultPort, timeout: Int = DefaultTimeout): BackendConnection = {
    try {
      val factory = supportedVersions(DefaultVersion)
      factory(host, port, timeout)
    } catch {
      case ex @ WrongMythProtocolException(requiredVersion) =>
        if (supportedVersions contains requiredVersion) {
          val factory = supportedVersions(requiredVersion)
          factory(host, port, timeout)
        }
        else throw new UnsupportedMythProtocolException(ex)
    }
  }
}
