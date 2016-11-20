package mythtv
package connection
package myth

import java.util.concurrent.locks.ReentrantLock

import scala.concurrent.duration.Duration

trait EventLock {
  def event: Option[Event]
  def isCancelled: Boolean
  def cancel(): Unit
  def waitFor(timeout: Duration = Duration.Inf): Unit
}

object EventLock {
  def apply(eventConn: EventConnection, eventFilter: (Event) => Boolean): EventLock =
    new Lock(eventConn, eventFilter)

  /**
    * Returns an event lock object without an event; waitFor always returns
    * immediately and event is always None.
    */
  def empty: EventLock = Empty

  private final class Lock(eventConn: EventConnection, eventFilter: (Event) => Boolean)
      extends EventListener with EventLock {
    private[this] val lock = new ReentrantLock
    private[this] val cond = lock.newCondition

    @volatile private[this] var cancelled: Boolean = false
    @volatile private[this] var unlockEvent: Option[Event] = None

    eventConn.addListener(this)

    def listenFor(event: Event): Boolean = eventFilter(event)

    def handle(event: Event): Unit = {
      unlockEvent = Some(event)
      signalDone()
    }

    def cancel(): Unit = {
      cancelled = true
      signalDone()
    }

    private def signalDone(): Unit = {
      lock.lock()
      try cond.signalAll()
      finally lock.unlock()
      eventConn.removeListener(this)
    }

    def event: Option[Event] = unlockEvent

    def isCancelled = cancelled

    private def continueWaiting: Boolean =
      unlockEvent.isEmpty && !cancelled

    def waitFor(timeout: Duration): Unit = {
      lock.lock()
      try {
        if (timeout.isFinite) {
          var nanos = timeout.toNanos
          while (nanos > 0 && continueWaiting)
            nanos = cond.awaitNanos(nanos)
        } else {
          while (continueWaiting)
            cond.await()
        }
      }
      finally lock.unlock()
    }
  }

  private object Empty extends EventLock {
    def event: Option[Event] = None
    def isCancelled: Boolean = false
    def cancel(): Unit = ()
    def waitFor(timeout: Duration): Unit = ()
  }
}
