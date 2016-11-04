package mythtv
package model

import java.time.LocalTime

import EnumTypes._
import util.MythDateTime

final case class RecordRuleId(id: Int) extends AnyVal

// TODO seems like this contains most of the elements of ProgramGuideEntry or Recordable or some such...
trait RecordRule {
  def id: RecordRuleId
  def recType: RecType
  def chanId: Option[ChanId]
  def startTime: MythDateTime
  def endTime: MythDateTime
  def title: String
  def subtitle: String
  def description: String
  def season: Option[Int]
  def episode: Option[Int]
  def category: String
  def recProfile: String
  def recPriority: Int
  def autoExpire: Boolean
  def maxEpisodes: Int
  def maxNewest: Boolean
  def startOffset: Int   // units: minutes
  def endOffset: Int     // units: minutes
  def recGroup: String
  def dupMethod: DupCheckMethod
  def dupIn: DupCheckIn
  def callsign: String   // NB called "station" in the record DB table
  def seriesId: Option[String]
  def programId: Option[String]
  def inetRef: Option[String]
  def searchType: RecSearchType
  def autoTranscode: Boolean
  def autoCommFlag: Boolean
  def autoUserJob1: Boolean
  def autoUserJob2: Boolean
  def autoUserJob3: Boolean
  def autoUserJob4: Boolean
  def autoMetadata: Boolean
  def findDay: Int      // TODO is this really a day-of-week integer?
  def findTime: Option[LocalTime]
  def inactive: Boolean
  def parentId: Option[RecordRuleId]
  def transcoder: Option[Int]   // TODO what type is this? (ugh, see what I do here in mythjango...)
  def playGroup: String
  def preferredInput: Option[InputId]
  def nextRecord: Option[MythDateTime]
  def lastRecord: Option[MythDateTime]
  def lastDelete: Option[MythDateTime]
  def storageGroup: String
  def averageDelay: Int   // TODO what units?
  def filter: Option[Int] // TODO what type is this? bitmask enum of data from the recordfilter table?

  override def toString: String = s"<RecordRule $id $title>"
}
