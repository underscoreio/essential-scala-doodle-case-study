## A Small Example

To make this concrete let's experiment with a version of the system we'll be building. You can find this system on the `feature/event` branch. We're going to use it to make a little ball move around the screen in response to key presses.

We start by converting the `Canvas` callbacks for animation frames and key presses into event streams.

```scala
import doodle.backend.Key
import doodle.core._
import doodle.event._
import doodle.jvm.Java2DCanvas

val redraw = Canvas.animationFrameEventStream(canvas)
val keys = Canvas.keyDownEventStream(canvas)
```

Now we're going to convert key presses into velocity. Velocity in a vector starting at (0, 0), and we'll increment or decrement it by one as appropriate on each key press. Additionally we going to limit the x and y components of velocity to be in the range -5 to 5. This stops the ball flying around the screen to quickly. 

```scala
val velocity = keys.foldp(Vec.zero)((key, prev) => {
      val velocity = 
        key match {
          case Key.Up    => prev + Vec(0, 1)
          case Key.Right => prev + Vec(1, 0)
          case Key.Down  => prev + Vec(0, -1)
          case Key.Left  => prev + Vec(-1, 0)
          case _         => prev
        }
      Vec(velocity.x.min(5).max(-5), velocity.y.min(5).max(-5))
  }
)
```

Now we update the location of the ball by the velocity. Location starts at (0,0) and this time we're limiting it to be within a 600 by 600 screen.

```scala
val location = velocity.foldp(Vec.zero){ (velocity, prev) =>
    val location = prev + velocity
    Vec(location.x.min(300).max(-300), location.y.min(300).max(-300))
  }
```

Finally we create the frames and render them.

```scala
val ball = Circle(20) fillColor (Color.red) lineColor (Color.green)

val frames = location.map(location => ball at location)
Canvas.animate(Java2DCanvas.canvas, frames)
```

If you play with this code you'll find it has an annoying problem: it only updates the ball's position on key presses. What we really want is to the ball continually moving around the screen. We can achieve this by joining the `velocity` stream with the `redraw` stream. The resulting stream will have a value everything there is a new value available on either `velocity` and `redraw`. Since `redraw` is updated 60 times a second (the screen refresh rate) this will give us a ball that moves around smoothly. The following redefinition of `location` is sufficient.

```scala
val location = redraw.join(velocity).map{ case(ts, m) => m }.
  foldp(Vec.zero){ (velocity, prev) =>
    val location = prev + velocity
    Vec(location.x.min(300).max(-300), location.y.min(300).max(-300))
  }
``` 
