package mythtv
package connection
package myth

import java.time.{ Duration, Instant, ZoneOffset }

//import FileStats  // TODO API exposed data types should be in model, not buried here
import model.{ ChanId, FreeSpace, Recording, RemoteEncoder, VideoPosition }
import util.{ ByteCount, ExpectedCountIterator, FileStats, MythDateTime }

// TODO these APIs should be converted to return Option[_] or Either[_] or something
trait MythProtocolAPI {
  def allowShutdown(): Boolean
  def blockShutdown(): Boolean
  def checkRecording(rec: Recording): Boolean
  def deleteRecording(rec: Recording): Int
  def done(): Unit
  def fillProgramInfo(playbackHost: String, p: Recording): Recording
  def forceDeleteRecording(rec: Recording): Int
  def forgetRecording(rec: Recording): Int   // TODO something better to indicate success/failure; Either?
  def getFreeRecorder: RemoteEncoder
  def getFreeRecorderCount: Int
  def getFreeRecorderList: List[Any]  // TODO see getFreeRecorder for return type
  def getNextFreeRecorder(encoderId: Int): Any // see above for return type
  def getRecorderFromNum(encoderId: Int): Any  // see above for return type
  def getRecorderNum(rec: Recording): Any      // see above for return type
  def goToSleep(): Boolean  // TODO a way to return error message if any
  def lockTuner(): Any // TODO capture the appropriate return type
  def lockTuner(cardId: Int): Any // see above for return type
  def protocolVersion(ver: Int, token: String): (Boolean, Int)
  def queryActiveBackends: List[String]
  def queryBookmark(chanId: ChanId, startTime: MythDateTime): VideoPosition
  def queryCommBreak(chanId: ChanId, startTime: MythDateTime): Long  // TODO List frame number/position
  def queryCutList(chanId: ChanId, startTime: MythDateTime): Long    // TODO List frame number/position
  def queryFileExists(fileName: String, storageGroup: String): (String, FileStats)
  def queryFileHash(fileName: String, storageGroup: String, hostName: String = ""): String
  def queryFreeSpace: List[FreeSpace]
  def queryFreeSpaceList: List[FreeSpace]
  def queryFreeSpaceSummary: (ByteCount, ByteCount)
  def queryGetAllPending: ExpectedCountIterator[Recording]
  def queryGetAllScheduled: ExpectedCountIterator[Recording]
  def queryGetConflicting: Iterable[Recording]  // TODO expected count iterator?
  def queryGetExpiring: ExpectedCountIterator[Recording]
  def queryGuideDataThrough: MythDateTime
  def queryHostname: String
  def queryIsActiveBackend(hostName: String): Boolean
  def queryIsRecording: (Int, Int)
  def queryLoad: (Double, Double, Double)
  def queryMemStats: (ByteCount, ByteCount, ByteCount, ByteCount)
  def queryPixmapLastModified(rec: Recording): MythDateTime
  def queryRecording(pathName: String): Recording
  def queryRecording(chanId: ChanId, startTime: MythDateTime): Recording
  def querySetting(hostName: String, settingName: String): Option[String]
  def queryTimeZone: (String, ZoneOffset, Instant)
  def queryUptime: Duration
  def refreshBackend: Boolean
  def scanVideos: Boolean
  def setBookmark(chanId: ChanId, startTime: MythDateTime, pos: VideoPosition): Boolean
  def setSetting(hostName: String, settingName: String, value: String): Boolean
  def shutdownNow(haltCommand: String = ""): Unit
  def stopRecording(rec: Recording): Int  // TODO better encapsulate return codes
  def undeleteRecording(rec: Recording): Boolean
  def undeleteRecording(chanId: ChanId, startTime: MythDateTime): Boolean
  // TODO more methods
}
