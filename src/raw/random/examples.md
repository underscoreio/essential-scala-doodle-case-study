## Examples

### Random Vectors 

The Doodle case study comes with a [Vec][vec] data type, representing a two-dimensional vector. Using your implementation of `Random` implement a method to create random vectors.

```scala
def randomVec(x: Random[Double], y: Random[Point]): Random[Vec] =
  ???
```

<div class="solution">
```tut:book
def randomVec(x: Random[Double], y: Random[Point]): Random[Vec] =
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
