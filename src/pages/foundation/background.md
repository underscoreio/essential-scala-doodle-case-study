## Background

Our goal is to make it easy to create interesting vector graphics and animations. The typical
imperative APIs for drawing vector graphics, such as the Java 2D API or the HTML Canvas, work at a very low level of abstraction. This buts a lot of the burden on the programmer. An example of such as an API is

``` scala
trait Canvas {
  def setStroke(stroke: Stroke): Unit
  def setFill(color: Color): Unit

  def stroke(): Unit
  def fill(): Unit

  def beginPath(): Unit
  def moveTo(x: Double, y: Double): Unit
  def lineTo(x: Double, y: Double): Unit
  def bezierCurveTo(cp1x: Double, cp1y: Double, cp2x: Double, cp2y: Double, endX: Double, endY: Double): Unit
  def endPath(): Unit
}
```

To draw a shape we call `beginPath`, then issue a series of commands (`moveTo`, `lineTo`, or, `bezierCurveTo`), followed by an `endPath`. We can draw just the outline of the shape, in which case we next call `stroke`, or just fill it (via `fill`), or do both. The colors used for the stroke and fill and set by calls to `setStroke` and `setFill`

For example, to draw a circle we could use the following method:

``` scala
def circle(centerX: Double, centerY: Double, radius: Double): Unit = {
  // See http://spencermortensen.com/articles/bezier-circle/ for approximation
  // of a circle with a Bezier curve.
  val c = 0.551915024494
  val cR = c * radius
  beginPath()
  moveTo(centerX, centerY + radius)
  bezierCurveTo(centerX + cR, centerY + radius,
                centerX + radius, centerY + cR,
                centerX + radius, centerY)
  bezierCurveTo(centerX + radius, centerY - cR,
                centerX + cR, centerY - radius,
                centerX, centerY - radius)
  bezierCurveTo(centerX - cR, centerY - radius,
                centerX - radius, centerY - cR,
                centerX - radius, centerY)
  bezierCurveTo(centerX - radius, centerY + cR,
                centerX - cR, centerY + radius,
                centerX, centerY + radius)
  endPath()
}
```

So what is wrong with this API? Before reading on, it would be good for you to think about issues you can see with it.

In my experience, there is a lot wrong with this sort of API. One problem is there is a lot of state involved. There is a single global stroke color, for example. This makes it difficult to abstract parts of an image into methods, as they can overwrite each other's stroke color. Similarly it's not defined what happens if we nest calls to `beginPath`, or if we call `stroke` or `fill` before calling `endPath`, and so on. All of this state makes it hard to create reusable components from which we can build up bigger pictures.

The use of a global coordinate system makes layout difficult. Imagine we have code to draw two pictures, and we now want to put these pictures side by side. First we had better have had the foresight to make the starting point of each picture a parameter to the method that draws them, or we won't be able to shift them around. Then we have to calculate our layout manually---work out how wide each picture is, and move them by an appropriate amount so they are balanced across the screen. This is a lot of donkey work, and doing donkey work is what computers are for. 
