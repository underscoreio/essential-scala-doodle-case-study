## Implementation

We're now going to implement `EventStream`. We will start with a very simple and somewhat broken implementation, and then explore improvements of increasing complexity.

### Mutable State

Our initial implementation will use mutable state.

Observability and equivalence. Unobservable state isn't a problem.


### Push-driven Implementation

Our implementation approach will be push-driven. This means that when a source receives an event, we will push that event to listening nodes, and so on up the graph till we reach a node with no listeners. The implication is that each node must store its listeners. Let's tackle this in small steps, starting by implementing `map`.

We have the interface

```scala
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B]
}
```

Start by somehow implementing `map` *without worrying about listeners*. Hint: use the same trick we used to implement layout for `Image`.

<div class="solution">
Just like we did for the layout combinators (`beside`, `on`, etc.) we can represent as data the computations we don't know how to actually before at this stage.

```scala
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B] =
    Map(f)
}
final case class Map[A,B](f: A => B) extends EventStream[B]
```

Note that `Map` extends `EventStream[B]`. Remember the type parameter indicates the type of events the stream produces. We need the extra parameter `A` to denote the type of events that `Map` accepts.
</div>

Now we're going to add in the listeners. These listeners are a collection (a `List` will do), but of what type? An `EventStream[A]` generates events of type `A` but it says nothing about the type of events it accepts as input, or if it event accepts input at all. We need another type to represent this. The `Observer` type will do

```scala
sealed trait Observer[A] {
  def update[A](in: A): Unit
}
```

Now we can specify that `Map` is an `Observer[A]` and an `EventStream[B]`, though we don't know how to implement `update` yet.

```scala
final case class Map[A,B](f: A => B) extends Observer[A] with EventStream[B] {
  def update[A](in: A): Unit =
    ???
}
```

Now we can say every `EventStream[A]` has a collection of listeners of type `Observer[A]`. It doesn't really matter what collection type we use, but we'll have to be able to update it---so it either needs to be a mutable collection or stored in a `var`. Implement this.

<div class="collection">
I've chosen to use a `List` stored in a `var`.

```scala
sealed trait Observer[A] {
  def update[A](in: A): Unit
}
sealed trait EventStream[A] {
  var listeners: List[Observer[A]]

  def map[B](f: A => B): EventStream[B] =
    Map(f)
}
final case class Map[A,B](f: A => B) extends EventStream[B]
```
</div>

Now when we `map` over an event stream we need to add a new listener. Implement this.

<div class="collection">
```scala
sealed trait Observer[A] {
  // We will eventually implement `update` with structural recursion as usual.
  // For now we're going to leave it unimplemented but still compiling.
  def update[A](in: A): Unit =
    ???
}
sealed trait EventStream[A] {
  var listeners: List[Observer[A]]

  def map[B](f: A => B): EventStream[B] = {
    val node = Map(f) 
    listeners = node +: listeners
    node
  }
}
final case class Map[A,B](f: A => B) extends Observer[A] with EventStream[B]
```
</div>

This is the basic implementation pattern we will use for the rest of the methods.

Implement `scanLeft`. Hint: you will need to introduce mutable state to store the previous output of `scanLeft`.

Implement `join`. Hint: `join` is an `Observer` of both types `A` and `B`. We can implement this in one class 

Add sources:
- source data type
- event propagation
