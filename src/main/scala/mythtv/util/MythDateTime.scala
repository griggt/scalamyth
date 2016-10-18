package mythtv
package util

import java.time.{ LocalDateTime, OffsetDateTime, Instant, ZoneId, ZoneOffset, ZonedDateTime }
import java.time.format.DateTimeFormatter

// TODO: do we need to/from RFC format (RFC_1123_DATE_TIME) ?

class MythDateTime(val instant: Instant) extends AnyVal {
  private def offsetDt = instant atOffset ZoneOffset.UTC

  // accessors
  def year: Int = offsetDt.getYear
  def month: Int = offsetDt.getMonthValue
  def day: Int = offsetDt.getDayOfMonth

  def hour: Int = offsetDt.getHour
  def minute: Int = offsetDt.getMinute
  def second: Int = offsetDt.getSecond

  // formatters
  import MythDateTime.FORMATTER_MYTH
  def mythformat: String = offsetDt format FORMATTER_MYTH
  def toMythFormat: String = mythformat
  def toIsoFormat: String = instant.toString
  def toNaiveIsoFormat: String = offsetDt format DateTimeFormatter.ISO_LOCAL_DATE_TIME
  override def toString: String = instant.toString

  // converters
  def toTimestamp: Long = instant.getEpochSecond
  def toInstant: Instant = instant
  def toLocalDateTime: LocalDateTime = offsetDt.toLocalDateTime   // is this naive UTC? TODO rename method?
  def toOffsetDateTime: OffsetDateTime = offsetDt
  def toZonedDateTime: ZonedDateTime = instant atZone ZoneOffset.UTC
}

object MythDateTime {
  final val FORMATTER_MYTH = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

  // TODO define this implicit here or in util package object?
  // Defining it here makes it hard to import on purpose, and prevents
  // "enrichment" like LocalDateTime.toMythFormat from easily working.
  /*
  import scala.language.implicitConversions
  implicit def javaInstant2MythDt(instant: Instant) = new MythDateTime(instant)
  implicit def javaLocalDt2MythDt(dt: LocalDateTime) = new MythDateTime(dt)
   */
  def apply(instant: Instant) = new MythDateTime(instant)

  def apply(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) =
    new MythDateTime(
      OffsetDateTime.of(year, month, day, hour, minute, second, 0, ZoneOffset.UTC).toInstant)

  private def apply(localDt: LocalDateTime) = new MythDateTime(localDt.toInstant(ZoneOffset.UTC))

  def fromMythFormat(mdt: String): MythDateTime = MythDateTime(LocalDateTime.parse(mdt, FORMATTER_MYTH))

  // this is strict ISO format with UTC ("Z") timezone
  def fromIso(isoDt: String): MythDateTime = MythDateTime(Instant.parse(isoDt))

  // this is naive ISO format (no timezone, implied to be UTC)
  def fromNaiveIso(isoDt: String): MythDateTime = MythDateTime(LocalDateTime.parse(isoDt))

  // Like naive ISO format but with space delimiter instead of 'T'.
  // Returned by backend as result of QUERY_GUIDEDATATHROUGH for example
  def fromNaiveIsoLoose(isoDt: String): MythDateTime =
    MythDateTime(LocalDateTime.parse(isoDt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]")))

  def fromTimestamp(ts: Long): MythDateTime = MythDateTime(Instant.ofEpochSecond(ts))

  def now: MythDateTime = MythDateTime(Instant.now)

  object MythDateTimeOrdering extends Ordering[MythDateTime] {
    def compare(x: MythDateTime, y: MythDateTime) = x.instant compareTo y.instant
  }

  implicit def ordering: Ordering[MythDateTime] = MythDateTimeOrdering
}
