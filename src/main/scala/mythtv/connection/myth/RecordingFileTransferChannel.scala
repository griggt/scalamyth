package mythtv
package connection
package myth

import model.{ ChanId, Recording }
import util.MythDateTime

// TODO only use this (recording in progress) file channel if we have called
// api.checkRecording(Recording) to verify that the recording is still in progress.
// Otherwise use a CompletedRecordingFileTransferChannel.  Don't expose these
// differences to clients; hide behind a RecordingFileTransferChannel trait.

// TODO: also listen for DoneRecording/FileClosed event
// TODO: listen for RecordingListUpdate event to update our copy of Recording object??

class RecordingFileTransferChannel private[myth](
  controlChannel: MythFileTransferAPI,
  dataChannel: FileTransferConnection,
  eventChannel: EventConnection,
  recording: Recording
) extends EventingFileTransferChannel(controlChannel, dataChannel, eventChannel) {

  override def listener: EventListener = updateListener

  // TODO block read if the recording is still in progress but we hit EOF? (wait for the event to arrive...)

  private[this] lazy val updateListener = new EventListener {
    override def listenFor(event: BackendEvent): Boolean = event.isEventName("UPDATE_FILE_SIZE")

    override def handle(event: BackendEvent): Unit = event.parse match {
      case Event.UpdateFileSizeEvent(chanId, startTime, newSize) =>
        if (chanId == recording.chanId && startTime == recording.startTime)
          currentSize = newSize
      case _ => ()
    }
  }
}

object RecordingFileTransferChannel {
  // TODO: method that takes a hostname vs control channel?
  def apply(api: BackendAPIConnection, chanId: ChanId, recStartTs: MythDateTime): RecordingFileTransferChannel = {
    val rec = api.queryRecording(chanId, recStartTs)   // TODO check for failure/not found
    val inProgress = api.checkRecording(rec)

    // TODO who is managing these opened connections??  Also, we have no re-use...

    val controlChannel =
      if (rec.hostname == api.host) api
      else BackendAPIConnection(rec.hostname)

    val dataChannel = FileTransferConnection(controlChannel.host, rec.filename, rec.storageGroup, port = controlChannel.port)
    val eventChannel = EventConnection(controlChannel.host, controlChannel.port)

    val fto = MythFileTransferObject(controlChannel, dataChannel)
    new RecordingFileTransferChannel(fto, dataChannel, eventChannel, rec)
  }
}
