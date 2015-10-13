## Atoms and Operations

Now we have thought about the properties of our system, let's think about the most basic classes and operations on those classes. By now you should have had a play with Doodle and have a good idea of it's model. In Doodle the "atoms" are basic geometric shapes like circles, rectangles, and triangles. The operations that combine the atoms are layout methods like `on` and `beside`, and methods to manipulate stroke and fill.

Design a data type to represent the "atoms" in your model. You should probably use an algebraic data type. Hint: You might want to look at the [Canvas](https://github.com/underscoreio/doodle-case-study/blob/master/shared/src/main/scala/doodle/backend/Canvas.scala), which is the low-level interface we'll implement our higher-level interface against.

<div class="solution">
We can start with a very simple model like so: "An `Image` is a `Circle` or a `Rectangle`". You should be immediately be able to translate this into code.

```scala
sealed trait Image
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height:  Double) extends Image
```

The low-level abstraction we are rendering to is built on paths. We could model this as, say "A `PathElement` is a `MoveTo`, `LineTo`, or `CurveTo`" and "A `Path` is a sequence of `PathElements`". This is actually the representation that Doodle uses, but we provide a higher-level interface like `Image` above for convenience. The `Path` interface can also be directly converted into code. (At this point in Essential Scala we haven't seen the `Seq` type yet. It represents a sequence of elements.)

```scala
sealed trait PathElement
final case class MoveTo(x: Double, y: Double) extends PathElement
final case class LineTo(x: Double, y: Double) extends PathElement
final case class CurveTo(cp1x: Double, cp1y: Double, cp2x: Double, cp2y: Double, endX: Double, endY: Double) extends PathElement

final case class Path(elements: Seq[PathElement])

final case class Image(path: Path)
```
</div> 

Now create the method signatures for the operations you'll define on your data type. You don't need to fill in the bodies yet---you can just leave then as `???` so the code compiles but won't run.

<div class="solution">
If you're following Doodle you will have methods similar to

```scala
sealed trait Image {
  def on(that: Image): Image =
    ???
  
  def beside(that: Image): Image =
    ???
  
  def above(that: Image): Image =
    ???
}
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height:  Double) extends Image
```

You might also want methods to add stroke and fill, but for the purposes of this case study we can leave them out for now.
</div>

You will also need a method `draw` that accepts a `doodle.backend.Canvas` and actually renders the images using the canvas. More on that in a moment.
