## Implementation

We're now ready to implement the complete system. We have provided a framework of code to build on, which you can find on [Github](https://github.com/underscoreio/doodle-case-study). The code you'll interact with can found within `src/main/scala/doodle`. Within this directory you'll find the following sub-directories:

- `core`, which is where most of your code should go, and where you'll find many useful utilities;
- `backend`, containing the `Canvas` interface;
- `example`, containing a few simple examples;
- `jvm`, containing an implementation of `Canvas` for the JVM's Java2D library; and
- `syntax`, which has a few utilities that you can ignore for now.


### Implementation Techniques

There is something that you might find a bit unexpected in the implementation of your library: nothing should be drawn until you call the `draw` method. This is necessary as we need to know the entire image before we can layout its components. Concretely, if we're rendering one image beside another, we need to know their heights so we can vertically center them. If we draw images as soon as they were created we won't know that they should be laid out in this way. The upshot is when we call, say, `image1 beside image2`, we need to represent this as a data structure somehow. 

The idea of separating the description of the computation (the image data structure) from the process that carries it out (drawing) is a classic functional programming technique, and one we will see multiple times. Being functional programming, we have a fancy word for this: *reification*. Reification means to make concrete something that was abstract. In our case we're turning what seems like an action (e.g. `beside`) into a concrete data structure.

When we come to do layout we need to know the height and width of each component image. We can easily calculate this with bounding boxes---they are easy to implement and sufficient if we only allow horizontal and vertical composition.


### Your Mission

Your final mission is to finish off the library:

- implement the methods for combining images; and
- implement `draw`

Along the way you will probably have to implement a bounding box abstraction.

If you're not sure where to start, follow along with the rest of this section. If you think you can do it yourself, get stuck in!

When you have finished, you can compare your implementation to ours by switching to the [feature/atoms-and-operations branch](https://github.com/underscoreio/doodle-case-study/tree/feature/atoms-and-operations) in your fork of the case study repository.

#### Drawing Something

Let's start with this representation:

```scala
import doodle.backend.Canvas

sealed trait Image {
  def on(that: Image): Image =
    ???
  
  def beside(that: Image): Image =
    ???
  
  def above(that: Image): Image =
    ???

  def draw(canvas: Canvas): Unit =
    ???
}
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height:  Double) extends Image
```

Our first mission is to get some visible progress, so we'll implement `draw`. We're completely ignoring layout at this point, so you can just draw images at the origin (or anywhere else that takes your fancy). 

What pattern will we use in the implementation?

<div class="solution">
`Image` is an algebraic data type, so we'll use structural recursion.
</div>

Implement `draw`.

<div class="solution">
Save this file as `Image.scala` in `shared/src/main/scala/doodle/core`

```scala
package doodle
package core

import doodle.backend.Canvas

sealed trait Image {
  def on(that: Image): Image =
    ???
  
  def beside(that: Image): Image =
    ???
  
  def above(that: Image): Image =
    ???

  def draw(canvas: Canvas): Unit =
    this match {
      case Circle(r)      => canvas.circle(0.0, 0.0, r)
      case Rectangle(w,h) => canvas.rectangle(-w/2, h/2, w/2, -h/2)
    }
}
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height:  Double) extends Image
```

With this in-place you should be able to render some simple images. From the sbt console, try

```scala
val canvas = Java2DCanvas.canvas
Circle(10).draw(canvas)
```
</div>

Now we can draw stuff on the screen, let's implement the methods to combine images: `above`, `beside`, and `on`. Each method can be implemented in one line, but there is a crucial leap you need to make to implement them. Have a think about it, and read the solution when you've worked it out. 

<div class="solution">
We need to represent the layout operations as data, which means we need to extend the `Image` algebraic data type with cases for layout. Then the method bodies just constructs the correct instance that represents the operation.

When we add these new cases to our algebraic data type we also need to add them to `draw` as well (as per the structural recursion pattern, but the compiler will complain in any case if we forget them.) Right now we're not actually doing any layout so we just recurse down the data structure and draw the leaves.

```scala
package doodle
package core

import doodle.backend.Canvas

sealed trait Image {
  def on(that: Image): Image =
    On(this, that)
  
  def beside(that: Image): Image =
    Beside(this, that)
  
  def above(that: Image): Image =
    Above(this, that)

  def draw(canvas: Canvas): Unit =
    this match {
      case Circle(r)      => canvas.circle(0.0, 0.0, r)
      case Rectangle(w,h) => canvas.rectangle(-w/2, h/2, w/2, -h/2)
      case Above(a, b)    => a.draw(canvas); b.draw(canvas)
      case On(o, u)       => o.draw(canvas); u.draw(canvas)
      case Beside(l, r)   => l.draw(canvas); r.draw(canvas)
    }
}
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height:  Double) extends Image
final case class Above(above: Image, below: Image) extends Image
final case class On(on: Image, under: Image) extends Image
final case class Beside(left: Image, right: Image) extends Image
```
</div>

Now we are ready to tackle the actual layout algorithm. To work out where every `Image` should be placed we need to know how much space it takes up. We can implement bounding boxes to give us this information. A bounding box is simply a rectangle that encloses an image. Bounding boxes are not precise, but they are sufficient for our choice of primitive images and layout methods.

When combining bounding boxes we will need to know the coordinate system we use to represent their coordinates. We can't use the global canvas coordinate system---the reason we're implementing this system is to work out the location of images in the global system---so we need to use a coordinate system that is local to each image. A simple choice is to say the origin is the center of the bounding box.

We can represent a bounding box as a class

```scala
final case class BoundingBox(left: Double, top: Double, right: Double, bottom: Double)
```

What methods should be have on `BoundingBox`?

<div class="solution">
We will want methods to combine bounding boxes that mirror the methods to combine `Images`. So, `above`, `beside`, and `on`. We might also find it useful to store the width and height.
</div>

Implement these methods on `BoundingBox`.

<div class="solution">
```scala
package doodle
package core

final case class BoundingBox(left: Double, top: Double, right: Double, bottom: Double) {
  val height: Double = top - bottom

  val width: Double = right - left

  def above(that: BoundingBox): BoundingBox =
    BoundingBox(
      this.left min that.left,
      (this.height + that.height) / 2,
      this.right max that.right,
      -(this.height + that.height) / 2
    )

  def beside(that: BoundingBox): BoundingBox =
    BoundingBox(
      -(this.width + that.width) / 2,
      this.top max that.top,
      (this.width + that.width) / 2,
      this.bottom min that.bottom
    )

  def on(that: BoundingBox): BoundingBox =
    BoundingBox(
      this.left min that.left,
      this.top max that.top,
      this.right max that.right,
      this.bottom min that.bottom
    )
}
```
</div>

Now implement a `boundingBox` method (or instance variable, as you see fit) on `Image` that returns the bounding box for the image.

<div class="solution">
More structural recursion! Note we can implement `boundingBox` as an instance variable as it is fixed for all time, and therefore we don't need to recalculate it.

```scala
package doodle
package core

import doodle.backend.Canvas

sealed trait Image {
  val boundingBox: BoundingBox =
    this match {
      case Circle(r) =>
        BoundingBox(-r, r, r, -r)
      case Rectangle(w, h) =>
        BoundingBox(-w/2, h/2, w/2, -h/2)
      case Above(a, b) =>
        a.boundingBox above b.boundingBox
      case On(o, u) =>
        o.boundingBox on u.boundingBox
      case Beside(l, r) =>
        l.boundingBox beside r.boundingBox
    }

  def on(that: Image): Image =
    On(this, that)

  def beside(that: Image): Image =
    Beside(this, that)

  def above(that: Image): Image =
    Above(this, that)

  def draw(canvas: Canvas): Unit =
    this match {
      case Circle(r)      => canvas.circle(0.0, 0.0, r)
      case Rectangle(w,h) => canvas.rectangle(-w/2, h/2, w/2, -h/2)
      case Above(a, b)    => a.draw(canvas); b.draw(canvas)
      case On(o, u)       => o.draw(canvas); u.draw(canvas)
      case Beside(l, r)   => l.draw(canvas); r.draw(canvas)
    }
}
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height:  Double) extends Image
final case class Above(above: Image, below: Image) extends Image
final case class On(on: Image, under: Image) extends Image
final case class Beside(left: Image, right: Image) extends Image
```
</div>

Now we have enough information to do layout. Our `Image` is a tree. The top level bounding box tells us how big the entire image is. We can decide the origin of this bounding box is the origin of the global canvas coordinate system. Then we can walk down the tree (yet more structural recursion) translating the local coordinate system into the global system. When we reach a leaf node (so, a primite image), we can actually draw it. We already have the skeleton for this in `draw`---we just need to pass along the mapping from the local coordinate system to the global one. We can use a method like

```scala
def draw(canvas: Canvas, originX: Double, originY: Double): Unit =
   ???
```

which we call from the standard `draw` with the origin coordinates initially set to zero.

Now implement this variant of `draw`.

<div class="solution">
Below is the complete code.

```scala
package doodle
package core

import doodle.backend.Canvas

sealed trait Image {
  val boundingBox: BoundingBox =
    this match {
      case Circle(r) =>
        BoundingBox(-r, r, r, -r)
      case Rectangle(w, h) =>
        BoundingBox(-w/2, h/2, w/2, -h/2)
      case Above(a, b) =>
        a.boundingBox above b.boundingBox
      case On(o, u) =>
        o.boundingBox on u.boundingBox
      case Beside(l, r) =>
        l.boundingBox beside r.boundingBox
    }

  def on(that: Image): Image =
    On(this, that)

  def beside(that: Image): Image =
    Beside(this, that)

  def above(that: Image): Image =
    Above(this, that)

  def draw(canvas: Canvas): Unit =
    draw(canvas, 0.0, 0.0)

  def draw(canvas: Canvas, originX: Double, originY: Double): Unit =
    this match {
      case Circle(r) =>
        canvas.circle(0.0, 0.0, r)
      case Rectangle(w,h) =>
        canvas.rectangle(-w/2, h/2, w/2, -h/2)
      case Above(a, b) =>
        val box  = this.boundingBox
        val aBox = a.boundingBox
        val bBox = b.boundingBox

        val aboveOriginY = originY + box.top - (aBox.height / 2)
        val belowOriginY = originY + box.bottom + (bBox.height / 2)

        a.draw(canvas, originX, aboveOriginY)
        b.draw(canvas, originX, belowOriginY)
      case On(o, u) =>
        o.draw(canvas, originX, originY)
        u.draw(canvas, originX, originY)
      case Beside(l, r) =>
        val box  = this.boundingBox
        val lBox = l.boundingBox
        val rBox = r.boundingBox

        val leftOriginX = originX + box.left  + (lBox.width / 2)
        val rightOriginX = originX + box.right - (rBox.width / 2)
        l.draw(canvas, leftOriginX, originY)
        r.draw(canvas, rightOriginX, originY)
    }
}
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height:  Double) extends Image
final case class Above(above: Image, below: Image) extends Image
final case class On(on: Image, under: Image) extends Image
final case class Beside(left: Image, right: Image) extends Image
```
</div>
