## Atoms and Operations

Now we have thought about the properties of our system, let's think about the most basic types, and the operations on those types. In other words, what is the basic API (application programming interface) that our system will provide? We don't need to write code right now, but it will help to be somewhat formal.

<div class="solution">
There is no right solution to this. Indeed we're hoping that your design will be different to ours, as this will allow you to compare different approaches and learn by doing so.

In Doodle the core type is `Image`. Basic images are geometric shapes like `Circle`, `Rectangle`, and `Triangle`.

Operations on `Image` include layout operations:

- `Image above Image = Image`
- `Image on Image = Image`
- `Image beside Image = Image`
- `Image at Vec = Image`

Here I'm writing how the operations work in terms of types. An `Image` on an `Image` returns an `Image`, for example.

There are also operations for changing the style:

- `Image fillColor Color = Image`
- `Image lineColor Color = Image`
- `Image lineWidth Double = Image`
</div>

Design a data type to represent the "atoms" in your model. You should probably use an algebraic data type. Hint: You might want to look at the [Canvas](https://github.com/underscoreio/doodle-case-study/blob/master/shared/src/main/scala/doodle/backend/Canvas.scala), which is the low-level interface we'll implement our higher-level interface against. This will help you determine what's possible in the context of the case study, and the types involved.

<div class="solution">
We can start with a very simple model like so: "An `Image` is a `Circle` or a `Rectangle`". You should be immediately be able to translate this into code.

```scala
sealed trait Image
final case class Circle(radius: Double) extends Image
final case class Rectangle(width: Double, height: Double) extends Image
```

The `Canvas` abstraction we are rendering to is built on paths. We could model this as, say "A `PathElement` is a `MoveTo`, `LineTo`, or `CurveTo`" and "A `Path` is a sequence of `PathElements`". This is actually the representation that Doodle uses, but we provide a higher-level interface like `Image` above for convenience. The `Path` interface can also be directly converted into code. (At this point in Essential Scala we haven't seen the `Seq` type yet. It represents a sequence of elements.)

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
