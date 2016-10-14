package mythtv
package connection
package myth

import java.time.{ Duration, Instant, ZoneOffset }

import model.{ CaptureCardId, CardInput, Channel, ChanId, FreeSpace, Recording, RemoteEncoder, UpcomingProgram, VideoPosition, VideoSegment }
import util.{ ByteCount, ExpectedCountIterator, FileStats, MythDateTime }
import model.EnumTypes.{ ChannelBrowseDirection, ChannelChangeDirection, PictureAdjustType }
import EnumTypes.MythProtocolEventMode

// TODO these APIs should be converted to return Option[_] or Either[_] or something
trait MythProtocolAPI {
  def allowShutdown(): Boolean
  def announce(mode: String, hostName: String = "", eventMode: MythProtocolEventMode = MythProtocolEventMode.None): Boolean
  def announceFileTransfer(hostName: String, fileName: String, storageGroup: String,
    writeMode: Boolean = false, useReadAhead: Boolean = true, timeout: Duration = Duration.ofSeconds(2)): (Int, ByteCount)
  // TODO SlaveBackend and FileTransfer announces (more complex)
  def blockShutdown(): Boolean
  def checkRecording(rec: Recording): Boolean
  def deleteFile(fileName: String, storageGroup: String): Boolean
  def deleteRecording(rec: Recording): Int
  def done(): Unit
  def fillProgramInfo(playbackHost: String, p: Recording): Recording
  def forceDeleteRecording(rec: Recording): Int
  def forgetRecording(rec: Recording): Int   // TODO something better to indicate success/failure; Either?
  def freeTuner(cardId: CaptureCardId): Boolean
  def getFreeRecorder: RemoteEncoder
  def getFreeRecorderCount: Int
  def getFreeRecorderList: List[CaptureCardId]
  def getNextFreeRecorder(cardId: CaptureCardId): RemoteEncoder
  def getRecorderFromNum(cardId: CaptureCardId): RemoteEncoder
  def getRecorderNum(rec: Recording): RemoteEncoder
  def goToSleep(): Boolean  // TODO a way to return error message if any
  def lockTuner(): Any // TODO capture the appropriate return type
  def lockTuner(cardId: CaptureCardId): Any // see above for return type
  def protocolVersion(ver: Int, token: String): (Boolean, Int)
  def queryActiveBackends: List[String]
  def queryBookmark(chanId: ChanId, startTime: MythDateTime): VideoPosition
  def queryCheckFile(rec: Recording, checkSlaves: Boolean = true): String
  def queryCommBreak(chanId: ChanId, startTime: MythDateTime): List[VideoSegment]
  def queryCutList(chanId: ChanId, startTime: MythDateTime): List[VideoSegment]
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
  def queryPixmapGetIfModified(maxFileSize: Long, rec: Recording): (MythDateTime, Option[PixmapInfo])
  def queryPixmapGetIfModified(modifiedSince: MythDateTime, maxFileSize: Long, rec: Recording): (MythDateTime, Option[PixmapInfo])
  def queryPixmapLastModified(rec: Recording): MythDateTime
  // TODO also implement all these queryRecorderXXX methods on the RemoteEncoder class? (we return one, see above)
  def queryRecorderCancelNextRecording(cardId: CaptureCardId, cancel: Boolean): Unit
  def queryRecorderChangeBrightness(cardId: CaptureCardId, adjType: PictureAdjustType, up: Boolean): Int
  def queryRecorderChangeChannel(cardId: CaptureCardId, dir: ChannelChangeDirection): Unit
  def queryRecorderChangeColour(cardId: CaptureCardId, adjType: PictureAdjustType, up: Boolean): Int
  def queryRecorderChangeContrast(cardId: CaptureCardId, adjType: PictureAdjustType, up: Boolean): Int
  def queryRecorderChangeHue(cardId: CaptureCardId, adjType: PictureAdjustType, up: Boolean): Int
  def queryRecorderCheckChannel(cardId: CaptureCardId, channum: String): Boolean
  def queryRecorderCheckChannelPrefix(cardId: CaptureCardId, channumPrefix: String): (Boolean, Option[CaptureCardId], Boolean, String)
  // This returns a map from frame number to duration, what is that???
  def queryRecorderFillDurationMap(cardId: CaptureCardId, start: VideoPosition, end: VideoPosition): Map[VideoPosition, Long]
  // This returns a map from frame number to file byte offset
  def queryRecorderFillPositionMap(cardId: CaptureCardId, start: VideoPosition, end: VideoPosition): Map[VideoPosition, Long]
  def queryRecorderFinishRecording(cardId: CaptureCardId): Unit
  def queryRecorderFrontendReady(cardId: CaptureCardId): Unit
  def queryRecorderGetBrightness(cardId: CaptureCardId): Int
  def queryRecorderGetChannelInfo(cardId: CaptureCardId, chanId: ChanId): Channel
  def queryRecorderGetColour(cardId: CaptureCardId): Int
  def queryRecorderGetContrast(cardId: CaptureCardId): Int
  def queryRecorderGetCurrentRecording(cardId: CaptureCardId): Recording
  def queryRecorderGetFilePosition(cardId: CaptureCardId): Long
  def queryRecorderGetFrameRate(cardId: CaptureCardId): Double
  def queryRecorderGetFramesWritten(cardId: CaptureCardId): Long
  def queryRecorderGetFreeInputs(cardId: CaptureCardId, excludedCards: CaptureCardId*): List[CardInput]
  def queryRecorderGetHue(cardId: CaptureCardId): Int
  def queryRecorderGetInput(cardId: CaptureCardId): String
  // This returns byte offset from the approximate keyframe position
  def queryRecorderGetKeyframePos(cardId: CaptureCardId, desiredPos: VideoPosition): Long
  def queryRecorderGetMaxBitrate(cardId: CaptureCardId): Long
  def queryRecorderGetNextProgramInfo(cardId: CaptureCardId, chanId: ChanId, dir: ChannelBrowseDirection,
    startTime: MythDateTime): UpcomingProgram
  def queryRecorderGetNextProgramInfo(cardId: CaptureCardId, channum: String, dir: ChannelBrowseDirection,
    startTime: MythDateTime): UpcomingProgram
  def queryRecorderGetRecording(cardId: CaptureCardId): Recording
  def queryRecorderIsRecording(cardId: CaptureCardId): Boolean
  def queryRecorderPause(cardId: CaptureCardId): Unit
  // NB Must call queryRecorderPause before queryRecorderSetChannel
  def queryRecorderSetChannel(cardId: CaptureCardId, channum: String): Unit
  def queryRecorderSetInput(cardId: CaptureCardId, inputName: String): String
  // NB the recordingState parameter is ignored by the backend implementation
  def queryRecorderSetLiveRecording(cardId: CaptureCardId, recordingState: Int): Unit
  def queryRecorderSetSignalMonitoringRate(cardId: CaptureCardId, rate: Int, notifyFrontend: Boolean): Boolean
  def queryRecorderShouldSwitchCard(cardId: CaptureCardId, chanId: ChanId): Boolean
  // TODO FIXME when I invoked spawnLiveTV during testing, it caused SIGABRT on the backend !!
  def queryRecorderSpawnLiveTV(cardId: CaptureCardId, usePiP: Boolean, channumStart: String): Unit
  def queryRecorderStopLiveTV(cardId: CaptureCardId): Unit
  def queryRecorderToggleChannelFavorite(cardId: CaptureCardId, channelGroup: String): Unit
  def queryRecording(pathName: String): Recording
  def queryRecording(chanId: ChanId, startTime: MythDateTime): Recording
  def queryRecordings(specifier: String = "Unsorted"): ExpectedCountIterator[Recording]
  def querySetting(hostName: String, settingName: String): Option[String]
  def querySGGetFileList(hostName: String, storageGroup: String, path: String): List[String]
  def querySGGetFileList(hostName: String, storageGroup: String, path: String, fileNamesOnly: Boolean): List[String]
  def querySGFileQuery(hostName: String, storageGroup: String, fileName: String): (String, MythDateTime, ByteCount)
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
