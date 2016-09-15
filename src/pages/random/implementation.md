## Implementation

We're now ready to implement our `Random` data type. You should have all the tools you need to do this, but look at the solution below if you need a hint.

<div class="solution">
You can use the same reification strategy we used for `Image` to implement `Random`.
</div>

When you've completed your implementation take a look at ours.

<div class="solution">
Our implementation is below. It uses the same reification strategy we used for `Image`. In this case it's slightly more complicated because we need to store `this` in `FlatMap` and `Map`.

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
