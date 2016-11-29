package mythtv

import java.net.InetAddress

import services._
import util.ServiceDiscovery
import connection.http.BackendServiceConnection
import connection.myth.FrontendConnection

object MythTV {
  def frontend(host: String): MythFrontend = new MythFrontend(host)
  def backend(host: String): MythBackend = new MythBackend(host)

  def service[A <: Service](host: String)(implicit factory: ServiceFactory[A]): A = factory(host)
  def service[A <: Service](host: String, port: Int)(implicit factory: ServiceFactory[A]): A = factory(host, port)

  private def parseHostNameFromServiceName(serviceName: String): String = {
    val pattern = """.+ on (.+)""".r
    serviceName match {
      case pattern(hostName) => hostName
      case _ => ""
    }
  }

  def discoverFrontends: Iterable[FrontendInfo] = {
    val frontends = ServiceDiscovery.discoverFrontends
    frontends map (f => new FrontendInfo {
      def hostName     = parseHostNameFromServiceName(f.name)
      def addresses    = f.addresses
      def servicesPort = f.port
      def remoteControlPort = FrontendConnection.DefaultPort
    })
  }

  // If there is more than one master backend advertised via mDNS, this may find
  // either of them; which one is undefined.
  def discoverMasterBackend: BackendInfo = {
    import connection.http.json.JsonServiceFactory._

    val backends = ServiceDiscovery.discoverBackends
    val trialBackend = backends.head // TODO check that we got some results

    val host = trialBackend.addresses.head.getHostAddress
    val port = trialBackend.port
    val myth = service[MythService](host, port)

    val masterIp = myth.getSetting("MasterServerIP").right.get
    val masterPort = myth.getSetting("MasterServerPort").right.get.toInt

    val masterAddr = InetAddress.getByName(masterIp)
    val discoveredMaster = backends find (_.addresses contains masterAddr)

    if (discoveredMaster.isEmpty) new BackendInfo {
      def hostName         = ""
      def addresses        = List(masterAddr)
      def mythProtocolPort = masterPort
      def servicesPort     = BackendServiceConnection.DefaultPort
    }
    else new BackendInfo {
      def hostName         = parseHostNameFromServiceName(discoveredMaster.get.name)
      def addresses        = discoveredMaster.get.addresses
      def mythProtocolPort = masterPort
      def servicesPort     = discoveredMaster.get.port
    }
  }
}
