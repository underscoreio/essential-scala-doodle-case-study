## Implementation

Here's the API we designed in the previous section.

```tut:book:silent
sealed trait Random[A] {
  def run(rng: scala.util.Random): A =
    ???

  def flatMap[B](f: A => Random[B]): Random[B] =
    ???
    
  def map[B](f: A => B): Random[B] =
    ???
    
  def zip[B](that: Random[B]): Random[(A,B)] =
    for {
      a <- this
      b <- that
    } yield (a, b)
}
object Random {
  val double: Random[Double] =
    ???
    
  val int: Random[Int] =
    ???
    
  /** Generate a value from a normal or Gaussian distribution. */
  val normal: Random[Double] =
    ???
    
  /** Create a Random value that always generates `in`. */
  def always[A](in: A): Random[A] =
    ???
}
```

We're now ready to implement our `Random` data type. You should have all the tools you need to do this, but look at the solution below if you need a hint.

<div class="solution">
We can implement `Random` using the same reification strategy we used for `Image`.
</div>

When you've completed your implementation compare it to ours.

<div class="solution">
Our implementation is below. It uses the same reification strategy we used for `Image`, converting `flatMap` and `map` into data structures (`FlatMap` and `Map` respectively). We add another case, `Primitive`, to store the basic "atoms" we build more complicated random processes from.

The `Map` case is not strictly necessary, as we could implement it in terms of `FlatMap` and `Primitive`.

```tut:book
object random {
  sealed trait Random[A] {
    def run(rng: scala.util.Random): A =
      this match {
        case Primitive(f) => f(rng)
        case FlatMap(r, f) => f(r.run(rng)).run(rng)
        case Map(r, f) => f(r.run(rng))
      }
  
    def flatMap[B](f: A => Random[B]): Random[B] =
      FlatMap(this, f)
      
    def map[B](f: A => B): Random[B] =
      Map(this, f)
      
    def zip[B](that: Random[B]): Random[(A,B)] =
      for {
        a <- this
        b <- that
      } yield (a, b)
  }
  object Random {
    val double: Random[Double] =
      Primitive(rng => rng.nextDouble())
      
    val int: Random[Int] =
      Primitive(rng => rng.nextInt())
      
    /** Generate a value from a normal or Gaussian distribution. */
    val normal: Random[Double] =
      Primitive(rng => rng.nextGaussian())
      
    /** Create a Random value that always generates `in`. */
    def always[A](in: A): Random[A] =
      Primitive(rng => in)
  }
  final case class Primitive[A](f: scala.util.Random => A) extends Random[A]
  final case class FlatMap[A,B](random: Random[A], f: A => Random[B]) extends Random[B]
  final case class Map[A,B](random: Random[A], f: A => B) extends Random[B]
}
```
</div>
