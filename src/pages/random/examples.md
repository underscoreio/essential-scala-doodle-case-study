## Examples

```tut:invisible
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
import random._
final case class Vec(x: Double, y: Double)
```

### Random Vectors 

The Doodle case study comes with a [Vec][vec] data type, representing a two-dimensional vector. Using your implementation of `Random` implement a method to create random vectors.

```scala
def randomVec(x: Random[Double], y: Random[Double]): Random[Vec] =
  ???
```

<div class="solution">
```tut:book
def randomVec(x: Random[Double], y: Random[Double]): Random[Vec] =
  (x zip y).map { case (x, y) => Vec(x, y) }
```
</div>


### Random Circles

Create a method that constructs a circle with a random radius.

```scala
def randomCircle(radius: Random[Double]): Random[Circle] =
  ???
```

<div class="solution">
```scala
def randomCircle(radius: Random[Double]): Random[Circle] =
  radius.map { r => Circle(r) }
```
</div>


### Random Walk

Implement a method that does one step of a random walk.

```scala
def randomWalk(point: Random[Vec], noise: Random[Vec]): Random[Vec] =
  ???
```

<div class="solution">
```tut:book
def randomWalk(point: Random[Vec], noise: Random[Vec]): Random[Vec] =
```
</div>

[vec]: https://github.com/underscoreio/doodle-case-study/blob/master/shared/src/main/scala/doodle/core/Vec.scala
