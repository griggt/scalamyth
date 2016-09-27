package mythtv
package connection
package http

import java.net.HttpURLConnection

case class XMLResponse(statusCode: Int, headers: HttpHeaders, xml: String) extends HttpResponse

/*abstract*/ class XMLConnection(protocol: String, host: String, port: Int)
    extends AbstractHttpConnection(protocol, host, port) {

  override def setupConnection(conn: HttpURLConnection): Unit = {
    conn.setRequestProperty("Accept", "text/xml, application/xml")
  }

  override def request(path: String): XMLResponse = super.request(path) match {
    case StreamHttpResponse(status, headers, stream) =>
      val xml = ""    // TODO need to parse out the XML
      XMLResponse(status, headers, xml)
  }
}

abstract class BackendXMLConnection(host: String, port: Int) extends XMLConnection("http", host, port)
    with BackendServiceProtocol with BackendXMLOperations {
  def hosts: List[String] = ???
  def keys: List[String] = ???
  def setting(key: String, hostname: Option[String] = None, default: Option[String] = None) = ???

}