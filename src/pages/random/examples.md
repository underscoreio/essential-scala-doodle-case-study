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
sealed trait Image {
  def above(that: Image): Image = ???
  def beside(that: Image): Image = ???
}
final case class Circle(r: Double) extends Image
final case class Triangle(w: Double, h: Double) extends Image
```

We'll now explore some of the fun things we can do with our new library, building up to the picture in [@fig:random:sierpinski-confection].

![A sierpinski triangle rendered in glorious confectionary colours and random choice of triangles or circles for the leaf images.](src/pages/random/sierpinski-confection.pdf+svg){#fig:random:sierpinski-confection}

### Random Choice

Add to the `Random` API the ability to make a random choice between two (or more, if you're feeling inspired) alternatives. There are many ways you could implement this, so part of the challenge is exploring different alternatives.

<div class="solution">

The simplest way we can think of is to add a constructor for a `Random[Boolean]`.

```tut:book:silent
val boolean: Random[Boolean] =
  Primitive(rng => rng.nextBoolean())
```

Slightly easier to use is a method that chooses between two alternatives like an `if` expression. We can implement it in terms of `boolean` above. This uses a Scala feature, called call-by-name parameters, that we haven't seen so far.

```tut:book:silent
def conditional[A](pred: => Boolean)(t: => A)(f: => A): Random[A] =
  boolean map { b =>
    if(b) t else f
  }
```

Finally we could implmement a method that will choose one of any number of elements it is passed with equal probability for each. Here we use another new Scala feature, varargs.

```tut:book:silent
def oneOf[A](elts: A*): Random[A] = {
  val length = elts.length
  Primitive { rng => 
    val index = rng.nextInt(length)
    elts(index)
  }
}
```

The `oneOf` method provides the most general interface.
</div>

### Circle or Square

Now using the API you build above, create a method

```tut:book:silent
def shape(size: Double): Random[Image] =
  ???
```

This method should create either a triangle or a circle based on a random choice. The triangle should have width `size` and the circle should have *diameter* `size`.

<div class="solution">
Here's one solution using the `boolean` interface.

```tut:book:silent
def shape(size: Double): Random[Image] = {
  for {
    b <- boolean
  } yield if(b) Triangle(size, size) else Circle(size / 2)
}
```

Using `oneOf` we can write much simpler code.

```tut:book:silent
def shape(size: Double): Random[Image] = 
  oneOf(Triangle(size, size), Circle(size / 2))
```
</div>


### Sierpinski Triangle

We're now ready to draw a Sierpinski triangle, though not yet with fun colors. The Sierpinski triangle is an example of a structural recursion over the natural numbers. We're familiar with structural recursion, but how does it work over the natural numbers (and what are the natural numbers)?

The natural numbers are the integers from 0 upwards. In other words 0, 1, 2, 3, ... We can define the natural numbers recursively as, a natural number `N` is

- `0`; OR
- `1 + M`, where `M` is a natural number.

This means the skeleton for structural recursion on the natural numbers is

```scala
def doSomething(n: Int) =
  this match {
    case 0 => ??? // Base case here
    case n => ??? doSomething(n - 1) // Recursive case here
  }
```

For example, to draw concentric circles we can write

```scala
def singleCircle(n: Int): Image =
  Circle(50 + 5 * n) lineColor (Color.red fadeOut (n / 20).normalized)

def concentricCircles(n: Int): Image =
  n match {
    case 0 => singleCircle(n)
    case n => singleCircle(n) on concentricCircles(n - 1)
  }
```

Implement the Sierpinski triangle using structural recursion over the natural numbers. Hint: the pattern for creating the Sierpinski triangle is `a on (b beside c)`.

<div class="solution">
Here's our implementation, which creates a Sier*pink*si triangle. The `size` parameter is optional but allows a bit more creative freedom.

```scala
def triangle(size: Double): Image = {
  println(s"Creating a triangle")
  Triangle(size, size) lineColor Color.magenta
}

def sierpinski(n: Int, size: Double): Image = {
  println(s"Creating a Sierpinski with n = $n")
  n match {
    case 0 =>
      triangle(size)
    case n =>
      val smaller = sierpinski(n - 1, size/2)
      smaller above (smaller beside smaller)
  }
}
```
</div>

Now we'll turn it up to eleven and create a Sierpinski triangle where the leaves are randomly circles or triangles. We can use `shape` that we defined earlier.

Implement

```tut:book:silent
def randomSierpinski(n: Int): Random[Image] =
  ???
```

Note that this method returns a `Random[Image]`, *NOT* an `Image`.

<div class="solution">
The trick here is working how to combine the `Random[Image]` in the recursive case. We find this easier to write using a for comprehension, rather than nested `flatMap` and `map` calls.

```tut:book:silent
def randomSierpinski(n: Int, size: Double): Random[Image] = {
  n match {
    case 0 =>
      shape(size)
    case n =>
      val smaller = randomSierpinski(n - 1, size/2)
      for {
        a <- smaller
        b <- smaller
        c <- smaller
      } yield a above (b beside c)
  }
}
```
</div>

### Random Colors

Now we're going to add the final step, randomising the color. We're not going to give directions here, leaving you to explore, but here are a few tips.

- It's much easier to work with colors in the `HSLA` (hue, saturation, lightness, and alpha) representation that the `RGBA` one.
- The `hue` parameter to the `HSLA` constructor is an angle that indicates how far around the color wheel we should turn. You can convert a number between 0.0 and 1.0 to an `Angle` using the `turns` syntax. 1.0 means a full turn around the circle, and 0.0 is no turn.
- The `saturation` parameter determines how intense the color is. Values between about 0.5 and 1.0 are good. Use the `normalized` syntax to create a `Normalized` from a `Double`.
- The `lightness` parameter determines how far between black and white the color. You probably want moderate values here.
- The final parameter, `alpha`, determines transparency. You can play with this if you want or just set it to 1.0.

Here's an example of constructing a color in the `HSLA` representation.

```scala
import doodle.syntax._

val hue = 0.5.turns // halfway around the color wheel, a cyan
val saturation = 0.7.normalized // slightly desaturated, a bit pastel
val lightness = 0.5.normalized // not too light, not too dark
val alpha = 1.0.normalized
val color = HSLA(hue, saturation, lightness, alpha)
```

Start by creating a method that will construct a `Random[Color]`.

<div class="solution">
There's no right answer here, but you might be interested to see how we made the reddish confectionary colors in [@fig:random:sierpinski-confection]. We transform `Random.double` to generate values within a smaller range.

Note that `reddish` is a `val`, not a `def`. A `Random[A]` represents a computation that when `run` will generate values of type `A`, so we don't need to use `def` to delay evaluation.

```scala
val reddish: Random[Color] = {
  val hue = Random.double map { d => (d - 0.5) * 0.2 }
  val saturation = Random.double map { s => s * 0.3 + 0.4 }
  val lightness = Random.double map { l => l * 0.3 + 0.3 }

  for {
    h <- hue
    s <- saturation
    l <- lightness
  } yield HSLA(h.turns, s.normalized, l.normalized, 1.normalized)
}
```
</div>

Now use your random color generator to a glorious Sierpinski image.

<div class="solution">
Here's the complete code that generates the image we saw at the start.

```scala
object RandomSierpinski {
  val reddish: Random[Color] = {
    val hue = Random.double map { d => (d - 0.5) * 0.2 }
    val saturation = Random.double map { s => s * 0.3 + 0.4 }
    val lightness = Random.double map { l => l * 0.3 + 0.3 }

    for {
      h <- hue
      s <- saturation
      l <- lightness
    } yield HSLA(h.turns, s.normalized, l.normalized, 1.normalized)
  }

  def triangle(size: Double): Image = {
    Triangle(size, size)
  }
  def circle(size: Double): Image = {
    Circle(size / 2)
  }

  def shape(size: Double): Random[Image] = {
    for {
      b <- Random.boolean
      s =  if(b) triangle(size) else circle(size)
      h <- reddish
    } yield s fillColor h
  }

  def sierpinski(n: Int, size: Double): Random[Image] = {
    n match {
      case 0 =>
        shape(size)
      case n =>
        val smaller = sierpinski(n - 1, size/2)
        for {
          a <- smaller
          b <- smaller
          c <- smaller
        } yield a above (b beside c)
    }
  }

  val image = sierpinski(5, 512).run(scala.util.Random)
}
```
</div>

[vec]: https://github.com/underscoreio/doodle-case-study/blob/master/shared/src/main/scala/doodle/core/Vec.scala
