## Type Classes

We're now ready to tackle the main pattern for which we use implicits: type classes.

When we designed our `EventStream` interface we drew inspiration from the existing API of `List`. It can be useful to be able to abstract over `List` and `EventStream`. If we defined such an API, we could write event processing algorithms in terms of this API, making them oblivious to the concrete implementation they run on. Then we could run our algorithms in real-time data using the `EventStream` API and over batch (offline) data using `Lists` without making an code changes.

This is a perfect application for type classes. We have two types (`EventStream` and `List`) that share a common interface but don't share a common useful supertype. In fact their are many other types that have an API much like `EventStreams` (in the standard library, `Option` and `Future` come to mind, while in Essential Scala we have implemented some of these methods for types like `Sum`). A few type classes would allow us to unify a whole load of otherwise different types, and allow us to talk in a more abstract way about operations over them.

So, what should our type classes be? We have briefly discussed functors---things that have a `map` method. What about `join` and `scanLeft`? Things that can be joined are called applicative functors, or just applicatives for short. Our scan operation has the same signature as `scanLeft` on a `List`. There is no standard type class so we'll create our own called `Scanable`. Finally we'll through `Monads` into the mix, even though `EventStream` doesn't have `flatMap` method, because they are so useful in other contexts.

We have an informal idea of the type classes. Now let's get specific. For a type `F[A]`

- A `Functor` has a method `map[B](fa: F[A])(f: A => B): F[B]`
- An `Applicative` is a `Functor` and has
  - `zip(fa: F[A], fb: F[B]): F[(A, B)]`
  - `point[A](a: A): F[A]`
- A `Monad` is a `Functor` and has
  - `flatMap[B](fa: F[A])(f: A => F[B]): F[B]`
  - `point[A](a: A): F[A]`
  Note you can implement `map` in terms of `flatMap` and `point`.
- A `Scanable` has a method `scanLeft[B](seed: B)(f: (A, B) => B): F[B]`

Implement these type classes, putting your code in a package `doodle.typeclasses`. Create type class instances (where you can) for `EventStream` and `List`. Put the `EventStream` instances in its companion object, and the `List` instances in `doodle.typeclasses`.

You will run into a problem doing this. Read on for the solution but *make sure you attempt the exercise before you do*.

You perhaps tried defining a type class like

```scala
trait Function[F] {
  def map[A,B](fa: F[A])(f: A => B): F[B]
}
```

and received an error like `error: F does not take type parameters`. To solve this problem we need to learn about kinds and higher-kinded types.

Kinds are like types for types. The describe the number of "holes" or parameters in a type. We distinguish between regular types like `Int` and `String `that have no holes, and *type constructors*  like `List` and `EventStream` that have holes that we can fill to produce types like `List[Int]` and `EventStream[Image]`. Type constructors are analogous to functions of a single parameter, operating on the type level rather than the value level.

When we write a generic type parameter like the `F` in `trait Functor[F]` we must also tell Scala its kind. As you've probably guessed, no extra annotation means a regular type. To indicate a type constructor taking a single parameter we would write `F[_]`. `F` is the name of the type constructor, and `[_]` indicates it has a single parameter or hole. For example `trait Functor[F[_]]`

The specifying a kind on a type variable is like giving a type declaration on a regular method parameter. Just like a parameter we don't repeat the kind when we use the type variable. For example, if we write

~~~ scala
trait Functor[F[_]]
~~~

this declares a type variable called `F` with kind `[_]` (so a type constructor with a single type parameter). When we use `F` we don't write the `[_]`. Here's an example:

~~~ scala
trait Functor[F[_]] {
  def map[A,B](fa: F[A])(f: A => B): F[B]
}
~~~

We must enable *higher kinds* to use this feature of Scala, by importing `scala.language.higherKinds`.

Here's the complete example for `Functor`.

~~~ scala
import scala.language.higherKinds

trait Functor[F[_]] {
  def map[A,B](fa: F[A])(f: A => B): F[B]
}
~~~

Using your new knowledge of higher kinded types, implement the rest of the type classes and the type class instances.

<div class="solution">
Once you have the hang of higher-kinded types you should find this fairly mechanical.

First the type classes themselves.

```scala
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

trait Scanable[F[_]] {
  def scanLeft[A,B](fa: F[A])(b: B)(f: (B,A) => B): F[B]
}
```

Now the instances

```scala
object ListInstances {
  implicit object list extends Functor[List] with Monad[List] with Applicative[List] with Scanable[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] =
      fa.map(f)
    def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] =
      fa.flatMap(f)
    def point[A](a: A): List[A] =
      List(a)
    def zip[A, B](fa: List[A])(fb: List[B]): List[(A, B)] =
      fa.zip(fb)
    def scanLeft[A,B](fa: List[A])(b: B)(f: (B,A) => B): List[B] =
      fa.scanLeft(b)(f)
  }
}

object EventStream {
  implicit object eventStream extends Functor[EventStream] with Monad[EventStream] with Applicative[EventStream]with Scanable[EventStream] {
    def map[A, B](fa: EventStream[A])(f: A => B): EventStream[B] =
      fa.map(f)
    def point[A](a: A): EventStream[A] =
      EventStream.now(a)
    def zip[A, B](fa: EventStream[A])(fb: EventStream[B]): EventStream[(A, B)] =
      fa.zip(fb)
    def scanLeft[A,B](fa: EventStream[A])(b: B)(f: (B,A) => B): EventStream[B] =
      fa.scanLeft(b)(f)
  }
}
```
</div>

For extra bonus points implement type class instances for normal types (i.e. for any type `A`). This is known as the identity monad / functor / applicative. Hint: types are not type constructors---they have the wrong kind! However you can get the compiler to consider types as type constructors by declaring a *type synonym* like `type Id[A] = A`.

<div class="solution">
```scala
object IdInstances {
  type Id[A] = A

  implicit object list extends Functor[Id] with Monad[Id] with Applicative[Id] with Scanable[Id] {
    def map[A, B](fa: Id[A])(f: A => B): Id[B] =
      f(fa)
    def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] =
      f(fa)
    def point[A](a: A): Id[A] =
      a
    def zip[A, B](fa: Id[A])(fb: Id[B]): Id[(A, B)] =
      (fa, fb)
    def scanLeft[A,B](fa: Id[A])(b: B)(f: (B,A) => B): Id[B] =
      f(b,fa)
  }
}
```
</div>

Why is the identity monad / functor /applicative useful?


<div class="solution">
It allows us to treat normal values as if they were monads etc. and hence abstract over code that uses "real" monads / functors / applicatives and code that doesn't. This often occurs when code is used in some contexts where it runs concurrently (e.g. in `Future`) and in other contexts where it doesn't. 
</div>

Go hog wild, and use your new found powers to write methods producing animations either as a `List` or an `EventStream`. This allows us to easily view individual frames (by producing a `List`) or to view the entire animations (by using an `EventStream`).


