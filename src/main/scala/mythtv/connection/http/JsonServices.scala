package mythtv
package connection
package http

import model._
import services.{ CaptureService, ChannelService, DvrService }
import util.{ MythDateTime, OptionalCount }

class JsonCaptureService(conn: BackendJSONConnection) extends MythJsonProtocol with CaptureService {
  def getCaptureCard(cardId: CaptureCardId): CaptureCard = {
    val response = conn.request(s"/$serviceName/GetCaptureCard?CardId=${cardId.id}")
    val root = response.json.asJsObject.fields("CaptureCard")
    root.convertTo[CaptureCard]
  }

  def getCaptureCardList(hostName: String, cardType: String): List[CaptureCard] = {
    // TODO support parameters
    val response = conn.request(s"/$serviceName/GetCaptureCardList")
    val root = response.json.asJsObject.fields("CaptureCardList")
    root.convertTo[List[CaptureCard]]
  }
}

class JsonChannelService(conn: BackendJSONConnection) extends MythJsonProtocol with ChannelService {
  def getChannelInfo(chanId: ChanId): Channel = {
    val response = conn.request(s"/$serviceName/GetChannelInfo?ChanID=${chanId.id}")
    val root = response.json.asJsObject.fields("ChannelInfo")
    root.convertTo[ChannelDetails]
  }

  def getChannelInfoList: List[Channel] = {
    val response = conn.request(s"/$serviceName/GetChannelInfoList")
    val root = response.json.asJsObject.fields("ChannelInfoList")
    val list = root.convertTo[MythJsonObjectList[ChannelDetails]]
    list.items
  }

  def getVideoSource(sourceId: ListingSourceId): ListingSource = {
    val response = conn.request(s"/$serviceName/GetVideoSource?SourceID=${sourceId.id}")
    val root = response.json.asJsObject.fields("VideoSource")
    root.convertTo[ListingSource]
  }

  def getVideoSourceList: List[ListingSource] = {
    val response = conn.request(s"/$serviceName/GetVideoSourceList")
    val root = response.json.asJsObject.fields("VideoSourceList")
    val list = root.convertTo[MythJsonObjectList[ListingSource]]
    list.items
  }

  def getXMLTVIdList: List[String] = ???
}

class JsonDvrService(conn: BackendJSONConnection) extends MythJsonProtocol with DvrService {
  //import MythJsonProtocol._  importing saves code space in 2.11 but is less elegant?

  def getRecorded(chanId: ChanId, startTime: MythDateTime): Program = {
    val response = conn.request(s"/$serviceName/GetRecorded?ChanId=${chanId.id}&StartTime=${startTime.toNaiveIsoFormat}")
    val root = response.json.asJsObject.fields("Program")
    root.convertTo[Program]
  }

  def getRecordedList(startIndex: Int, count: OptionalCount[Int], descending: Boolean): List[Program] = {
    // TODO support parameters
    val response = conn.request(s"/$serviceName/GetRecordedList")
    val root = response.json.asJsObject.fields("ProgramList")
    val list = root.convertTo[MythJsonObjectList[Program]]
    list.items
  }

  def getExpiringList(startIndex: Int, count: OptionalCount[Int]): List[Program] = {
    // TODO support parameters
    val response = conn.request(s"/$serviceName/GetExpiringList")
    val root = response.json.asJsObject.fields("ProgramList")
    val list = root.convertTo[MythJsonObjectList[Program]]
    list.items
  }

  def getUpcomingList(startIndex: Int, count: OptionalCount[Int], showAll: Boolean): List[Program] = {
    val response = conn.request(s"/$serviceName/GetUpcomingList")
    val root = response.json.asJsObject.fields("ProgramList")
    val list = root.convertTo[MythJsonObjectList[Program]]
    list.items
  }

  def getConflictList(startIndex: Int, count: OptionalCount[Int]): List[Program] = ???

  def getEncoderList: List[RemoteEncoderState] = {
    val response = conn.request(s"/$serviceName/GetEncoderList")
    val root = response.json.asJsObject.fields("EncoderList")
    root.convertTo[List[RemoteEncoderState]]
  }

  def getRecordScheduleList(startIndex: Int, count: OptionalCount[Int]): List[RecordRule] = {
    // TODO support parameters
    val response = conn.request(s"/$serviceName/GetRecordScheduleList")
    val root = response.json.asJsObject.fields("RecRuleList")
    val list = root.convertTo[MythJsonObjectList[RecordRule]]
    list.items
  }

  def getRecordSchedule(recordId: RecordRuleId): RecordRule = {
    val response = conn.request(s"/$serviceName/GetRecordSchedule?RecordId=${recordId.id}")
    val root = response.json.asJsObject.fields("RecRule")
    root.convertTo[RecordRule]
  }
}