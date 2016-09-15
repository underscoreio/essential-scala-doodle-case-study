## A Random API

We'll represent our API with a type `Random`. Our basic and very incomplete model is

```scala
sealed trait Random {
  def run(rng: scala.util.Random): ???
}
```

where the `run` method is what we call to actually create a random value. How should we modify `Random` to give a sensible result type to `run`?

<div class="solution">
We need to add a type variable to `Random` to represent the type of value that the `Random` instance will produce when it is run.

```scala
sealed trait Random[A] {
  def run(rng: scala.util.Random): A =
    ???
}
```
</div>


### Independent and Conditional Distributions

What other methods do we want on `Random`? Probability distributions are well studied and we know we can build them using two tools:

- independent distributions; and
- conditional distributions.

What do these mean? Let's focus on conditional distributions, because we can express independent distributions using them.

A conditional distribution is a distribution that is defined in terms of some input value. Concretely, imagine we are implementing a process to render a random walk, as depicted in [@fig:random:brownian-motion]. In this process the next point to render is located at the current point plus some random noise. 

```scala
def walk(current: Point): Random[Point] = {
  // This code won't work but it illustrative of what we want to achieve.
  current + noise 
}
```

At each step we'll start with a `Random[Point]`, which we'd like to compose with `walk` above, giving

```scala
Random[Point] ??? walk = Random[Point]
// Expand the type of walk
Random[Point] ??? (Point => Random[Point]) = Random[Point]
```

With what method should we rpelace `???` to make this type equation hold?

<div class="solution">
If we replace `???` with `flatMap` the type equation holds.

```scala
Random[Point] flatMap (Point => Random[Point]) = Random[Point]
```
</div>

So it seems we can implement conditional distributions using `flatMap`. If we have `flatMap` what other method should we have?

<div class="solution">
At a minimum, we should have `map`, to complete the monad API.
</div>

Let's return to independent distributions now. Independent distributions are those that have no dependency between one another. A simple example is generating a point, where we generate the x- and y-coordinates independently. If `Random.double` generates a random `Double`, we might generate a random point with code like

```scala
val randomPoint: Random[Point] =
  (Random.double zip Random.double).map { (pt: (Double, Double)) =>
    val (x, y) = pt
    Point(x, y)
  }
```

This assumes we have a method `zip` on `Random` with type

```scala
def zip[B](that: Random[B]): Random[(A,B)] = 
  ???
```

Implement `zip` using `flatMap` and `map`.


<div class="solution">
Here's a sample solution.

```tut:book
sealed trait Random[A] {
  def run(rng: scala.util.Random): A =
    ???

  def flatMap[B](f: A => Random[B]): Random[B] =
    ???
    
  def map[B](f: A => B): Random[B] =
    ???
    
  def zip[B](that: Random[B]): Random[(A,B)] =
    this flatMap { a => 
      that map { b => 
        (a, b)
      }
    }
}
```

You might recognise that the `flatMap` / `map` pattern can be implemented with a for comprehension as follows.

```tut:book
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
```
</div>


### Other Methods

What other methods should we have, particularly on the companion object of `Random`?

<div class="solution">
At a minimum we need some constructors to create `Random` instances. We had an example above of `Random.double`. If we look at the methods on `scala.util.Random`, that might inspire some other constructors. For example, we could define

```scala
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
</div>
