## Implementation

We're now going to implement `EventStream`. We will start with a very simple and somewhat broken implementation, and then explore improvements of increasing complexity.

### Mutable State

Our initial implementation will use mutable state.

Observability and equivalence. Unobservable state isn't a problem.


### Push-driven Implementation

Our implementation approach will be push-driven. This means that when a source receives an event, we will push that event to observing nodes, and so on up the graph till we reach a node with no observers. The implication is that each node must store its observers. Let's tackle this in small steps, starting by implementing `map`.

We have the interface

```scala
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B]
}
```

Start by somehow implementing `map` *without worrying about observers*. Hint: use the same trick we used to implement layout for `Image`.

<div class="solution">
Just like we did for the layout combinators (`beside`, `on`, etc.) we can represent as data the computations we don't know how to actually implement at this stage.

```scala
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B] =
    Map(f)
}
final case class Map[A,B](f: A => B) extends EventStream[B]
```

Note that `Map` extends `EventStream[B]`. Remember the type parameter indicates the type of events the stream produces. We need the extra parameter `A` to denote the type of events that `Map` accepts.
</div>

Now we're going to add in the observers. These observers are a collection (a `List` will do), but of what type? An `EventStream[A]` generates events of type `A` but it says nothing about the type of events it accepts as input, or if it even accepts input at all. We need another type to represent this. The `Observer` type will do

```scala
sealed trait Observer[A] {
  // We'll implement this later.
  def observe(in: A): Unit =
    ???
}
```

Now we can specify that `Map` is an `Observer[A]` and an `EventStream[B]`, though we don't know how to implement `observe` yet.

```scala
final case class Map[A,B](f: A => B) extends Observer[A] with EventStream[B] 
```

Now we can say every `EventStream[A]` has a collection of observers of type `Observer[A]`. It doesn't really matter what collection type we use, but we'll have to be able to update it---so it either needs to be a mutable collection or stored in a `var`. Implement this.

<div class="solution">
I've chosen to use a mutable `ListBuffer`, but you could equally use a `List` stored in a `var`.

```scala
sealed trait Observer[A] {
  def observe(in: A): Unit =
    ???
}
sealed trait EventStream[A] {
  import scala.collection.mutable

  val observers: mutable.ListBuffer[Observer[A]] =
    new mutable.ListBuffer()

  def map[B](f: A => B): EventStream[B] =
    Map(f)
}
final case class Map[A,B](f: A => B) extends EventStream[B]
```
</div>

When we `map` over an event stream we need to add a new observer to the `EventStream`. Implement this.

<div class="solution">
```scala
sealed trait Observer[A] {
  def observe(in: A): Unit =
    ???
}
sealed trait EventStream[A] {
  import scala.collection.mutable

  val observers: mutable.ListBuffer[Observer[A]] =
    new mutable.ListBuffer()

  def map[B](f: A => B): EventStream[B] = {
    val node = Map(f) 
    observers += node 
    node
  }
}
final case class Map[A,B](f: A => B) extends Observer[A] with EventStream[B]
```
</div>

This is the basic implementation pattern we will use for the rest of the methods. Before we implement the rest of the API let's get `observe` working. What are we going to do in `observe`? Our only concrete implementation is `Map`. In `Map` we want to transform the input using the function `f` and then push that output to all observers. We might (rightly) worry about the order in which we push the output, but for now we'll ignore that question---any order will do.

<div class="solution">
We can implement `observe` using structural recursion. At this point we're not worried about the order in which we update the observers, so I've chosen left-to-right traversal. Since each call to `observe` will recursively result in another update, this choice also gives us depth-first traversal of the graph.

```scala
sealed trait Observer[A] {
  def observe(in: A): Unit =
    this match {
      case m @ Map(f) =>
        val output = f(in)
        m.observers.foreach(o => o.observe(output))
    }
}
sealed trait EventStream[A] {
  import scala.collection.mutable

  val observers: mutable.ListBuffer[Observer[A]] =
    new mutable.ListBuffer()

  def map[B](f: A => B): EventStream[B] = {
    val node = Map(f) 
    observers += node 
    node
  }
}
final case class Map[A,B](f: A => B) extends Observer[A] with EventStream[B]
```

You might feel some unease about this solution. A `Map` has both an input (of type `A`) and an output (of type `B`) but `Observer` only represents the input type. Nonetheless, in the `observe` method we are implicitly making use of the output type `B` when we bind `output` to the result of `f(in)` and then call `observe` on `m`'s observers.

Can we even write down a type for `output`? We can, using a feature called *existential types*. An existential type represents a specific type that we don't know. Here the existential would represent the unknown type `B`. Unfortunately there is a [compiler bug](https://issues.scala-lang.org/browse/SI-6680) that means the compiler actually infers `Any` in this situation.

Let's choose a different solution that doesn't introduce existential types and doesn't trigger this compiler bug. We'll introduce a type to represent a transformation that has both an input and an output---in other words `Observer[A] with EventStream[B]`---and define our structural recursion in this type where both input and output types are available.

This design also allows us to hide our mutable state (the observers) from users of the library. We haven't seen access modifiers in Scala yet, so a quick summary is in order. Scala has protected and private modifiers, like Java, but the meanings are slightly different. The good news is you can forget about the nuance. As we don't use traditional OO inheritance and overriding very often in Scala the details of access modifiers aren't very important. There is only modifier I have ever used in Scala, and that is `private[packageName]`, which makes a definition visible only within the named package. Below I've made `Node` private to the `event` package.

```scala
package doodle.event

sealed trait Observer[A] {
  def observe(in: A): Unit 
}
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B]
}
private[event] sealed trait Node[A,B] extends Observer[A] with EventStream[B] {
  import scala.collection.mutable

  val observers: mutable.ListBuffer[Observer[B]] =
    new mutable.ListBuffer()

  def observe(in: A): Unit =
    this match {
      case m @ Map(f) =>
        val output = f(in)
        m.observers.foreach(o => o.observe(output))
    }

  def map[C](f: B => C): EventStream[C] = {
    val node = Map(f)
    observers += node
    node
  }
}
final case class Map[A,B](f: A => B) extends Node[A,B]
```
</div>

Implement `scanLeft`. Hint: you will need to introduce mutable state to store the previous output of `scanLeft` that gets fed back into `scanLeft` when the next event arrives.

<div class="solution">
We can use the same pattern as `map` to implement `scanLeft`.

```scala
package doodle.event

sealed trait Observer[A] {
  def observe(in: A): Unit 
}
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B]
  def scanLeft[B](seed: B)(f: (B,A) => B): EventStream[B]
}
private[event] sealed trait Node[A,B] extends Observer[A] with EventStream[B] {
  import scala.collection.mutable

  val observers: mutable.ListBuffer[Observer[B]] =
    new mutable.ListBuffer()

  def observe(in: A): Unit =
    this match {
      case m @ Map(f) =>
        val output = f(in)
        m.observers.foreach(o => o.observe(output))
      case s @ ScanLeft(seed, f) =>
        val output = f(seed, in)
        s.seed = output
        s.observers.foreach(o => o.observe(output))
    }

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
}
final case class Map[A,B](f: A => B) extends Node[A,B]
final case class ScanLeft[A,B](var seed: B, f: (B,A) => B) extends Node[A,B]
```
</div>

### Infrastructure

We still have to implement `join`, but this is trickiest part of the API. Instead of tackling it now let's work on some of the supporting infrastructure so we can get some simple animations working.

Our first step is to implement event sources, which form the beginning of an event processing graph. In our implementation an event source simply pushes its input unchanged to its observers. We need to do two things:

- a data type to represent event sources; and
- a utility function to convert a callback handler to an event source.

A callback handler is a method that allows us to set a callback that is invoked when an event arrives. The `Canvas` trait has two callback handlers, shown below.

```scala
  /** Set a callback that will be called when the canvas is ready to display to a
    * new frame. This will generally occur at 60fps. There can only be a single
    * callback registered at any one time and there is no way to cancel a
    * callback once it is registered.
    *
    * The callback is passed a monotically increasing value representing the
    * current time in undefined units.
    */
  def setAnimationFrameCallback(callback: Double => Unit): Unit

  /** Set a callback when a key is pressed down. */
  def setKeyDownCallback(callback: Key => Unit): Unit
```

To represent event sources we can simply reuse `Map` passing in the identity function. (Note that we could define another case in our algebraic data type. I'm avoiding doing so to skirt around a type inference problem that we will crash into later. I encourage you to attempt this alternative implementation to see the error, which we will learn how to solve when we discuss implementing `join`.)

We now need to implement our utility to construct a source from a callback handler. Make it so.

<div class="solution">
The task here is a bit underspecified, to leave it open to explore alternative designs. My design puts this method on the companion object of `EventStream`.

```scala
object EventStream {
  def fromCallbackHandler[A](handler: (A => Unit) => Unit) = {
    val stream = new Map[A,A](identity _)
    handler((evt: A) => stream.observe(evt))
    stream
  }
}
```

We can use it like so:

```scala
val canvas = Java2DCanvas.canvas
val source = EventStream.fromCallbackHandler(canvas.setAnimationFrameCallback _)
```
</div>

With this in place you should be able to implement some simple animations.

### Join

We are now ready to tackle `join`, the trickiest part of the system. Join takes two event streams as input, and produces a tuple of the most recent events from both streams whenever *either* stream produces an event.

All our previous nodes have had a single input, whereas join has two. Therefore we can't use the same implementation strategy as before. We also need some mutable state, so we can record the most recent event from each stream we're observing.

Let first implement a class that will hold the mutable state.

```scala
private [event] class MutablePair[A,B](var l: A, var r: B)
```

I've made the class private to the `event` package so we can hide this implementation detail to the outside.

Now we can implement join. Here's the start of the definition I used. See if you can implement it yourself given this start. Note that you will probably run into a compilation issue that you won't be able to solve. Read the solution for more on this.

```scala
final case class Join[A,B]() extends Node[(A,B),(A,B)]
```

<div class="solution">
The trick is to realise we'll have to connect the inputs to `Join` some other way than having `Join` implement `observe` twice for both types `A` and `B`. (This won't work---consider `A` and `B` could be the same concrete type.) Then the implementation proceeds much as before. Here's my version, which doesn't compile but does follow the patterns we've used so far.

```scala
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

  def observe(in: A): Unit =
    this match {
      case m @ Map(f) =>
        val output = f(in)
        m.observers.foreach(o => o.observe(output))
      case s @ ScanLeft(seed, f) =>
        val output = f(seed, in)
        s.seed = output
        s.observers.foreach(o => o.observe(output))
      case j @ Join() =>
        j.observers.foreach(o => o.observe(in))
    }

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
final case class Map[A,B](f: A => B) extends Node[A,B]
final case class ScanLeft[A,B](var seed: B, f: (B,A) => B) extends Node[A,B]
final case class Join[A,B]() extends Node[(A,B),(A,B)] {
  val state: MutablePair[Option[A],Option[B]] = new MutablePair(None, None)

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
```

This gives the mysterious compilation error

```scala
EventStream.scala:32: error: constructor cannot be instantiated to expected type;
 found   : doodle.event.Join[A(in class Join),B(in class Join)]
 required: doodle.event.Node[A(in trait Node),B(in trait Node)]
      case j @ Join() =>
               ^
one error found
```

We have inadvertantly veered away from what Scala's type inference algorithm can handle. The source of the problem is the use of type variables in `Join` (specifically the `extends Node[(A,B),(A,B)]` part.) Here we are mixing type variables with a concrete type (a tuple), rather than simply passing them up to `Node` as we have done with `Map` and `ScanLeft`. This more complex construction is called a generalized algebraic datatype (GADT for short.)

Scala's type inference algorithm can't work out that `A` and `B` in `Node` are equivalent to `(A,B)` when `Node` is a `Join`. The solution is to abandon pattern matching, and implement our structural recursion with polymorphism. We have a bit more duplication as a result, but our code actually compiles. This seems a reasonable trade-off.

```scala
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
```
</div> 
