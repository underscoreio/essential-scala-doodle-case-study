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

<div class="collection">
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

Now when we `map` over an event stream we need to add a new observer. Implement this.

<div class="collection">
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
We can implement `observe` using structural recursion.

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
</div>

Implement `scanLeft`. Hint: you will need to introduce mutable state to store the previous output of `scanLeft` that gets fed back into `scanLeft`.

<div class="scala">
```scala
sealed trait Observer[A] {
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
final case class ScanLeft[A,B](var seed: B, f: (A, B) => B) extends Observer[A] with EventStream[B]
```
</div>

Implement `join`. This is tricky.

- GADTs.

Add sources:
- source data type
