package doodle.event

sealed trait Observer[A] {
  def observe(in: A): Unit 
}
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B]
  def scanLeft[B](seed: B)(f: (B,A) => B): EventStream[B]
}
object EventStream {
  def fromCallbackHandler[A](handler: (A => Unit) => Unit) = {
    val stream = new Map[A,A](identity _)
    handler((evt: A) => stream.observe(evt))
    stream
  }
}
private[event] sealed trait Node[A,B] extends Observer[A] with EventStream[B] {
  import scala.collection.mutable

  val observers: mutable.ListBuffer[Observer[B]] =
    new mutable.ListBuffer()

  def map[C](f: B => C): EventStream[C] = {
    val node = Map(f)
    observers += node
    node
  }

  def scanLeft[C](seed: C)(f: (C,B) => C): EventStream[C] = {
    val node = ScanLeft(seed, f)
    observers += node
    node
  }

  def join[C](that: EventStream[C]): EventStream[(B,C)] = {
    val node = Join[B,C]()
    this.map(b => node.updateLeft(b))
    that.map(c => node.updateRight(c))
    node
  }
}
final case class Map[A,B](f: A => B) extends Node[A,B] {
  def observe(in: A): Unit = {
    val output = f(in)
    observers.foreach(o => o.observe(output))
  }
}
final case class ScanLeft[A,B](var seed: B, f: (B,A) => B) extends Node[A,B] {
  def observe(in: A): Unit = {
    val output = f(seed, in)
    seed = output
    observers.foreach(o => o.observe(output))
  }
}
final case class Join[A,B]() extends Node[(A,B),(A,B)] {
  val state: MutablePair[Option[A],Option[B]] = new MutablePair(None, None)

  def observe(in: (A,B)): Unit = {
    observers.foreach(o => o.observe(in))
  }

  def updateLeft(in: A) = {
    state.l = Some(in)
    state.r.foreach { r => this.observe( (in,r) ) }
  }

  def updateRight(in: B) = {
    state.r = Some(in)
    state.l.foreach { l => this.observe( (l,in) ) }
  }
}

private [event] class MutablePair[A,B](var l: A, var r: B)
