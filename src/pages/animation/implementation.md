## Implementation

We're now going to implement `Stream`. 
We're going to tackle the implementation in a number of steps:

1. We will start by ignoring concurrency, and assuming all our data arrives from an `Iterator` (a built-in Scala type). With this assumption, and by reducing our interface a little bit, we can quickly build a working system.

2. We will add `scanLeft`, which requires we add state and leads to a slighly more complicated implementation.

3. We will add concurrency, which makes our implementation substantially more complicated but makes it useful for our end-goal: animating shapes.

A bit of terminology will be useful.
We will say that data flows from *source* to *sink*.
Moving from source to sink is moving *downstream*, and the reverse is going *upstream*.


### Basics

Let's start by implementing the following API:

```tut:silent:book
object streamWrapper {
sealed trait Stream[A] {
  def map[B](f: A => B): Stream[B]

  def zip[B](that: Stream[B]): Stream[(A,B)]
  
  def runFold[B](zero: B)(f: (B, A) => B): B
}
object Stream {
  def fromIterator[A](source: Iterator[A]): Stream[A] =
    ???
}
}; import streamWrapper._
```

Notice that we've dropped `scanLeft`, and we've renamed `join` to `zip`.
We'll see why later.

The API has the following components:

- we create a `Stream` using `fromIterator`;
- we transform a `Stream` using `map` and `zip`; and
- we run a `Stream` using `runFold`.

You can implement this using exactly the same technique we used with `Image`:

- reify the majority of the API; and
- implement an "interpreter" in `runFold`.

We encourage you to try this on your own before reading our solution.
Our solution is broken into stages so you can refer to the different parts if you get stuck.

First we reify the API.
Remember that reification means "turn into data".
This basically means creating an algebraic data type.

<div class="solution">
```tut:silent:book
object streamWrapper {
sealed trait Stream[A] {
  import Stream._

  def zip[B](that: Stream[B]): Stream[(A,B)] =
    Zip(this, that)

  def map[B](f: A => B): Stream[B] =
    Map(this, f)

  def runFold[B](zero: B)(f: (B, A) => B): B =
    ???
}
object Stream {
  def fromIterator[A](source: Iterator[A]): Stream[A] =
    FromIterator(source)

  def always[A](element: A): Stream[A] =
    FromIterator(Iterator.continually(element))

  def apply[A](elements: A*): Stream[A] =
    FromIterator(Iterator(elements: _*))

  // Stream algebraic data type
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
}
}; import streamWrapper._
```
</div>

Now we can implement the interpreter in `runFold`.
We want to process elements one at a time, like we will in the full system, so the straightforward structural recursion approach won't work. (Try it and see---you'll end up processing all elements at once.)
We will first implement a method `next` that gets the next element from the `Stream`.
This is a structural recursion so is straightforward to implement.

<div class="solution">
```tut:silent:book
object streamWrapper {
sealed trait Stream[A] {
  import Stream._
  // etc ...

  def runFold[B](zero: B)(f: (B, A) => B): B = {
    def next[A](stream: Stream[A]): A =
      stream match {
        case FromIterator(source) => source.next()
        case Map(source, f) => f(next(source))
        case Zip(left, right) => (next(left), next(right))
      }
  
    ???
  }
}
object Stream {
  // etc ...
  
  // Stream algebraic data type
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
}
}; import streamWrapper._
```
</div>

Now we implement `runFold` using `next`.

<div class="solution">
```tut:silent:book
object streamWrapper {
sealed trait Stream[A] {
  import Stream._
  // etc ...

  def runFold[B](zero: B)(f: (B, A) => B): B = {
    def next[A](stream: Stream[A]): A =
      stream match {
        case FromIterator(source) => source.next()
        case Map(source, f) => f(next(source))
        case Zip(left, right) => (next(left), next(right))
      }
  
    // Never terminates
    def loop(result: B): B = {
      loop(f(result, next(this)))
    }
  
    loop(zero)
  }
}
object Stream {
  // etc ...
  
  // Stream algebraic data type
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
}
}; import streamWrapper._
```
</div>

```tut:invisible
object streamWrapper {
sealed trait Stream[A] {
  import Stream._

  def zip[B](that: Stream[B]): Stream[(A,B)] =
    Zip(this, that)

  def map[B](f: A => B): Stream[B] =
    Map(this, f)

  def runFold[B](zero: B)(f: (B, A) => B): B = {
    def next[A](stream: Stream[A]): A =
      stream match {
        case FromIterator(source) => source.next()
        case Map(source, f) => f(next(source))
        case Zip(left, right) => (next(left), next(right))
      }

    // Never terminates
    def loop(result: B): B = {
      loop(f(result, next(this)))
    }

    loop(zero)
  }
}
object Stream {
  def fromIterator[A](source: Iterator[A]): Stream[A] =
    FromIterator(source)

  def always[A](element: A): Stream[A] =
    FromIterator(Iterator.continually(element))

  def apply[A](elements: A*): Stream[A] =
    FromIterator(Iterator(elements: _*))

  // Stream algebraic data type
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
}
}; import streamWrapper._
```

This code compiles but has one flaw: we never check if our `Iterator` has more data (using the `hasNext` method.)
Try running, for example,

```tut:fail:book
Stream(1, 2, 3).runFold(0)(_ + _)
```

Why has our method created broken code?
It's because `Iterator` is a stateful abstraction and not an algebraic data type, so the type system and our usual techniques don't work for us when using `Iterator`.
We have to rely on testing and our memory to get it right.

Let's modify `next` to return an `Option` indicating if more data is available, and add the correct check to the `FromIterator` case in `next`.

<div class="solution">
The full working code is

```tut:silent:book
object streamWrapper {
sealed trait Stream[A] {
  import Stream._

  def zip[B](that: Stream[B]): Stream[(A,B)] =
    Zip(this, that)

  def map[B](f: A => B): Stream[B] =
    Map(this, f)

  def runFold[B](zero: B)(f: (B, A) => B): B = {
    // Use `Option` to indicate if the stream has terminated.
    // `None` indicates no more values are available.
    def next[A](stream: Stream[A]): Option[A] =
      stream match {
        case FromIterator(source) =>
          if(source.hasNext) Some(source.next()) else None
        case Map(source, f) =>
          next(source).map(f)
        case Zip(left, right) =>
          for {
            l <- next(left)
            r <- next(right)
          } yield (l, r)
      }

    def loop(result: B): B =
      next(this) match {
        case None => result
        case Some(a) =>
          loop(f(result, a))
      }

    loop(zero)
  }
}
object Stream {
  def fromIterator[A](source: Iterator[A]): Stream[A] =
    FromIterator(source)

  def always[A](element: A): Stream[A] =
    FromIterator(Iterator.continually(element))

  def apply[A](elements: A*): Stream[A] =
    FromIterator(Iterator(elements: _*))

  // Stream algebraic data type
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
}
}; import streamWrapper._
```
</div>


### Adding State

Now we're going to add `scanLeft`.
Remember the signature for `scanLeft` is

```scala
def scanLeft[B](seed: B)(f: (B,A) => B): Stream[B]
```

Implementing `scanLeft` requires we keep state between successive calls in our interpreter.
If we reify `scanLeft` and add state we'll break substitution (try it and see!)
One way to implement a better system is to transform `Stream` into another representation, and that representation can contain state.
Because this internal representation is entirely hidden within `runFold` it can never be observed from outside the system and hence it's ok to use.

Here's how we did it.

<div class="solution">
`Stream` is defined mostly as before but now we convert it to `Observable` within `runFold`.

```tut:silent:book
object streamWrapper {
sealed trait Observable[A]{
  def runFold[B](zero: B)(f: (B, A) => B): B = {
    def next[A](observable: Observable[A]): Option[A] =
      stream match {
        case FromIterator(source) =>
          if(source.hasNext) Some(source.next()) else None
        case Map(source, f) =>
          next(source).map(f)
        case Zip(left, right) =>
          for {
            l <- next(left)
            r <- next(right)
          } yield (l, r)
        case s @ ScanLeft(source, z, f) =>
          s.zero = f(z, next(source))
          s.zero
      }

    def loop(result: B): B =
      next(this) match {
        case None => result
        case Some(a) =>
          loop(f(result, a))
      }

    loop(zero)
  }
}
object Observable {
  def fromStream(source: Stream[A]): Observable[A] = {
    source match {
      case Stream.Map(source, f) => Map(fromStream(source), f)
      case Stream.ScanLeft(source, zero, f) => ScanLeft(fromStream(source), zero, f)
      case Stream.Zip(left, right) => Zip(fromStream(left), fromStream(right))
      case Stream.FromIterator(handler) => Source(source)
    }
  
  // Observable algebraic data type
  final case class ScanLeft[A,B](source: Observable[A], var zero: B, f: (B,A) => B) 
  final case class Zip[A,B](left: Observable[A], right: Observable[B]) extends Observable[(A,B)]
  final case class Map[A,B](source: Observable[A], f: A => B) extends Observable[B]
  final case class FromIterator[A](source: Iterator[A]) extends Observable[A]
}

sealed trait Stream[A] {
  import Stream._

  def zip[B](that: Stream[B]): Stream[(A,B)] =
    Zip(this, that)

  def map[B](f: A => B): Stream[B] =
    Map(this, f)
    
  def scanLeft[B](zero: B)(f: (B, A) => B): Stream[B]

  def runFold[B](zero: B)(f: (B,A) => B): B = {
    Observable.fromStream(this).runFold(zero)(f)
  }
}
object Stream {
  def fromIterator[A](source: Iterator[A]): Stream[A] =
    FromIterator(source)

  def always[A](element: A): Stream[A] =
    FromIterator(Iterator.continually(element))

  def apply[A](elements: A*): Stream[A] =
    FromIterator(Iterator(elements: _*))

  // Stream algebraic data type
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
}
}; import streamWrapper._
```
</div>

This solution has a lot of repetition but it's easy code to read and write, as it's still using all our familiar patterns.


### Adding Concurrency

We're now going to add concurrency to our system, meaning our inputs will arrive over time.
This entails two changes:

- our inputs will come from other threads so we need to worry about concurrent access; and
- we'll implement new methods that deal with joining concurrent streams.

#### Input

Our input will arrive from callbacks.
We need to add a new way to create `Streams`, passing in a callback handler with which the interpreter will register a callback.
Concretely this means adding to the `Stream` companion object the method

```tut:silent:book
def fromCallbackHandler[A](handler: (A => Unit) => Unit): Stream[A] =
  ???
```

It's easy enough to reify this method but what should we do when the callback is called? 
We should assume that the callback will be called from a different thread, which means we'll need to store the data somewhere till we're ready to use it.
There is also the possibility of a race condition: we could try to read and write the data at the same time.
To guard against this we need to use a data structure that is safe for concurrent access.
A `java.util.concurrent.ArrayBlockingQueue` is a simple choice.

Our implementation is as follows:

- When an `Observable` `FromCallbackHandler` is constructured it registers a callback with the provided handler. This callback stores any value it receives into an `ArrayBlockingQueue`.

- When `next` processes a `FromCallbackHandler` it `takes` value from the `ArrayBlockingQueue`.

In a real system we'd want a more flexible implementation, to allow the user to specify the exact queuing semantics (e.g. how big should our queue be, and what is our behaviour on the putting side when the queue is full?)

For our purposes the following implementation does the job.

<div class="solution">
```tut:silent:book
object streamWrapper {
sealed trait Observable[A]{
  def runFold[B](zero: B)(f: (B, A) => B): B = {
    def next[A](observable: Observable[A]): Option[A] =
      stream match {
        case FromIterator(source) =>
          if(source.hasNext) Some(source.next()) else None
        case FromCallbackHandler(h, q) =>
          Some(q.take()) 
        case Map(source, f) =>
          next(source).map(f)
        case Zip(left, right) =>
          for {
            l <- next(left)
            r <- next(right)
          } yield (l, r)
        case s @ ScanLeft(source, z, f) =>
          s.zero = f(z, next(source))
          s.zero
      }

    def loop(result: B): B =
      next(this) match {
        case None => result
        case Some(a) =>
          loop(f(result, a))
      }

    loop(zero)
  }
}
object Observable {
  import java.util.concurrent.{BlockingQueue, ArrayBlockingQueue}

  def fromStream(source: Stream[A]): Observable[A] = {
    source match {
      case Stream.Map(source, f) => Map(fromStream(source), f)
      case Stream.ScanLeft(source, zero, f) => ScanLeft(fromStream(source), zero, f)
      case Stream.Zip(left, right) => Zip(fromStream(left), fromStream(right))
      case Stream.FromIterator(source) => FromIterator(source)
      case Stream.FromCallbackHandler(handler) => FromCallbackHandler(handler)
    }
  
  // Observable algebraic data type
  final case class ScanLeft[A,B](source: Observable[A], var zero: B, f: (B,A) => B) 
  final case class Zip[A,B](left: Observable[A], right: Observable[B]) extends Observable[(A,B)]
  final case class Map[A,B](source: Observable[A], f: A => B) extends Observable[B]
  final case class FromIterator[A](source: Iterator[A]) extends Observable[A]
  final case class FromCallbackHandler[A](
    handler: (A => Unit) => Unit,
    queue: BlockingQueue = new ArrayBlockingQueue(1)) extends Observable[A] {
    handler { a =>
      queue.put(a)
    }
  }
}

sealed trait Stream[A] {
  import Stream._

  def zip[B](that: Stream[B]): Stream[(A,B)] =
    Zip(this, that)

  def map[B](f: A => B): Stream[B] =
    Map(this, f)
    
  def scanLeft[B](zero: B)(f: (B, A) => B): Stream[B]

  def runFold[B](zero: B)(f: (B,A) => B): B = {
    Observable.fromStream(this).runFold(zero)(f)
  }
}
object Stream {
  def fromIterator[A](source: Iterator[A]): Stream[A] =
    FromIterator(source)
    
  def fromCallbackHandler[A](handler: (A => Unit) => Unit): Stream[A] =
    FromCallbackHandler(handler)

  def always[A](element: A): Stream[A] =
    FromIterator(Iterator.continually(element))

  def apply[A](elements: A*): Stream[A] =
    FromIterator(Iterator(elements: _*))

  // Stream algebraic data type
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
  final case class FromCallbackHandler[A](handler: (A => Unit) => Unit) extends Stream[A]
}
}; import streamWrapper._
```
</div>


#### Concurrent Joins

To be continued ...
