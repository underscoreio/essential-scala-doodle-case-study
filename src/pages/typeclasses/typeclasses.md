# Type Classes

How should we got about testing animations? We might want to programatically assert properties of an our animations, such as that they stay within a certain region, or manually inspect individual frames. This is difficult with our current setup.

To drive our `EventStreams` we need a source to generate events. We could create a dummy event stream that generate events as quickly as possible, but we still have the issue of stopping the events at some point and collecting and inspecting the intermediate results. Now consider that very little about the reactive programs we have written is actually tied to `EventStreams`. The `EventStream` API is almost identical to that of sequences in the collections library. Most of our code could be just as well be written to use, say, `List`, and we can easily inspect the output of a sequence of `List` transforms.

In this section we going to solve this problem by weilding our new tool: type classes.

Ex1. `EventStream` and `List` don't have any common useful superclass, not should they. What Scala pattern should we use to express they share a common interface?

Ex2. What name can we give to the common interface(s) that `EventStream` and `List` share?

Ex3. Implement these interfaces, and give implementations for `EventStream` and `List`. You will run into a problem doing this. To solve this problem we need to learn about kinds and higher-kinded types. 

Kinds are like types for types. The describe the number of "holes" or parameters in a type. We distinguish between regular types like `Int` and `String `that have no holes, and *type constructors*  like `List` and `EventStream` that have holes that we can fill to produce types like `List[Int]` and `EventStream[Image]`. Type constructors are analogous to functions of a single parameter, operating on the type level rather than the value level.

When we write a generic type parameter like the `A` in `class Foo[A]` we must also tell Scala its kind. As you've probably guessed, no extra annotation means a regular type. To indicate a type constructor taking a single parameter we would write `F[_]`. `F` is the name of the type constructor, and `[_]` indicates it has a single parameter or hole. For example `class Foo[F[_]]`

The specifying a kind on a type variable is like giving a type declaration on a regular method parameter. Just like a parameter we don't repeat the kind when we use the type variable. For example, if we write

~~~ scala
class Foo[F[_]]
~~~

this declares a type variable called `F` with kind `[_]` (so a type constructor with a single type parameter). When we use `F` we don't write the `[_]`. Here's an example:

~~~ scala
class Foo[F[_], A](fa: F[A])
~~~

We must enable *higher kinds* to use this feature of Scala, by importing `scala.language.higherKinds`.

Here is a very simple example of an identity function that expects a higher kinded type.

~~~ scala
import scala.language.higherKinds

def higherIdentity[F[_], A](in: F[A]): F[A] = in
~~~

Using your new knowledge of higher kinded types, implement these interfaces we have been discussing. If you don't remember the precise interface, check the answer to Q2.

Ex4: For extra bonus points implement these interfaces for normal types (i.e. for any type `A`). This is known as the identity monad / functor / applicative. Hint: types are not type constructors---they have the wrong kind! However you can get the compiler to consider types as type constructors by declaring a *type synonym* like `type Id[A] = A`.

Ex5: Why is the identity monad / functor /applicative useful?

Ex6: Go hog wild, and use your new found powers to write methods producing animations either as a `List` or an `EventStream`. This allows us to easily view individual frames (by producing a `List`) or to view the entire animations (by using an `EventStream`).

## Answers

Ex1: A type class

Ex2: `map` implies a `Functor`. `flatMap` implies a `Monad`. `zip` implies an `Applicative` (an abstraction not discussed in Essential Scala). Note that we treat these abstractions very informally in Essential Scala. There are also laws or identities that instances of these abstractions should fulfil. Furthermore, `Monads` and `Applicatives` have a function `point(a: A): F[A]`. Finally, this treatment of `Applicative` is slightly different to the usual one in the literature.

Let's unpack that a bit.

For a type `F[A]`

- A `Functor` has a function `map[B](fa: F[A])(f: A => B): F[B]`
- A `Monad` is a `Functor` and has
  - `flatMap[B](fa: F[A])(f: A => F[B]): F[B]`
  - `point[A](a: A): F[A]`
  Note you can implement `map` in terms of `flatMap` and `point`.
- An `Applicative` is a `Functor` and has
  - `zip(fa: F[A], fb: F[B]): F[(A, B)]`
  - `point[A](a: A): F[A]`

Ex3:

Here are the interfaces.

~~~ scala
import scala.language.higherKinds

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Monad[F[_]] extends Functor[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def point[A](a: A): F[A]
}

trait Applicative[F[_]] extends Functor[F] {
  def zip[A, B](fa: F[A])(fb: F[B]): F[(A, B)]
  def point[A](a: A): F[A]
}
~~~

Our implementations could look like this.

~~~ scala
object ListInstances {
  implicit object list extends Functor[List] with Monad[List] with Applicative[List] {
    def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] =
      fa.flatMap(f)
    def point[A](a: A): List[A] =
      List(a)
    def zip[A, B](fa: List[A])(fb: List[B]): List[(A, B)] =
      fa.zip(fb)
  }
}

object EventStreamInstances {
  implicit object list extends Functor[EventStream] with Monad[EventStream] with Applicative[EventStream] {
    def flatMap[A, B](fa: EventStream[A])(f: A => EventStream[B]): EventStream[B] =
      fa.flatMap(f)
    def point[A](a: A): EventStream[A] =
      EventStream.once(a)
    def zip[A, B](fa: EventStream[A])(fb: EventStream[B]): EventStream[(A, B)] =
      fa.zip(fb)
  }
}
~~~

Ex4:

~~~ scala
object IdInstances {
  type Id[A] = A

  implicit object list extends Functor[Id] with Monad[Id] with Applicative[Id] {
    def map[A, B](fa: Id[A])(f: A => B): Id[B] =
      f(fa)
    def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] =
      f(fa)
    def point[A](a: A): Id[A] =
      a
    def zip[A, B](fa: Id[A])(fb: Id[B]): Id[(A, B)] =
      (fa, fb)
  }
}
~~~

Ex5:

It allows us to treat normal values as if they were monads etc. and hence abstract over code that uses "real" monads / functors / applicatives and code that doesn't. This often occurs when code is used in some contexts where it runs concurrently (e.g. in `Future`) and in other contexts where it doesn't. 
