package mythtv
package connection
package http
package xml

import java.net.HttpURLConnection

import scala.xml.XML

case class XmlResponse(statusCode: Int, headers: HttpHeaders, root: scala.xml.Elem) extends HttpResponse

trait XmlConnection extends AbstractHttpConnection {
  override def setupConnection(conn: HttpURLConnection): Unit = {
    conn.setRequestProperty("Accept", "text/xml, application/xml")
  }

  override def request(path: String): XmlResponse = super.request(path) match {
    case StreamHttpResponse(status, headers, stream) =>
      XmlResponse(status, headers, XML.load(stream))
  }
}

class BackendXmlConnection(protocol: String, host: String, port: Int)
  extends BackendServiceConnection(protocol, host, port)
    with XmlConnection {
  def this(host: String, port: Int) = this(BackendServiceConnection.DefaultProtocol, host, port)
  def this(host: String) = this(BackendServiceConnection.DefaultProtocol, host, BackendServiceConnection.DefaultPort)
}