## Easing Functions

Consider animating a UI element, such as a toolbar that slides out. We could simply linearly interpolate the position of the toolbar between the start and end points, but this simple movement lacks visual appeal. UI designers like to use movement that is more natural looking. For example, our toolbar's movement might accelarate over time, and it might bounce when it reaches it's final destination.

Functions that interpoloate position over time are known as *tweening functions* or *easing functions*. The most commonly used easing functions come from the work of [Robert Penner][penner]. His easing functions include include linear functions, quadratics and higher order polynomials, and more complication equations that give oscillating behaviour. (If you've studied physics you are probably thinking of damped spring models right now. That's not a bad place to start.)

In this section we are going to build a library of easing functions. This is a good example of functional design, and it will help us make more interesting animations with our animations library.

### The Easing Function 

In our representation, an easing function will accept and return a number between 0 and 1. The input is the *time*, from 0% to 100%. The output indicates position between 0% and 100% of the final position. The output should always start at 0 and finish at 1, but can vary arbitrarily inbetween. The more mathematically inclined will recognise this as the parametric form of a path. A quadratic easing function can then be defined as *f(t) = t^2*, for *t in [0, 1]*.

The obvious representation in Scala is to use a function `Double => Double`. However, not all `Double => Double` functions will be easing functions, and we'll want to add domain specific methods to our easing functions. This is makes sense to wrap our `Double => Double` functions in a type like[^unboxing]

~~~ scala
final case class Easing(get: Double => Double)
~~~

We should also implement some basic easing functions. Here are some equations to use:

- the linear function *f(t) = t*;
- the quadratic *f(t) = t^2*; and
- the "elastic" function *f(t) = 2^(-10t) * sin( (t-p/4) * 2pi / p) + 1* where *p = 0.3*

Implement these functions. You might it useful to import `scala.math`. Now implement an appropriate `apply` method on `Easing`.

### Composing Easing Functions

Just like Bilbo, we sometimes want to go there and back again when animating objects. An *ease in* function goes from 0 to 1 ait's input goes from 0 to 1. An *ease out* function is the reverse, going from 1 to 0 as input varies from 0 to 1. The functions we have implemented above are all ease in functions.

Given an ease in function we can construct an ease out. How? We can run it backwards and take the output away from 1. So if *f* is an ease in function, *g(t) = 1 - f(1-t)* is the corresponding ease out.

How should we represent this in code? We can easily add a method `easeOut` to `Easing`, transforming an ease in to an ease out. What would the result type of this method be? If it's an `Easing` we could apply `easeOut` again, which yields a broken `Easing`. Hmmm...

A better solution is to rename out type `Easing` to `EaseIn` and create a new type `EaseOut`. Add a method `easeOut` to `EaseIn`, and a method `easeIn` to `EaseOut`. These methods should be inverses.

Implement this.

Given some basic functions we can compose new tweening functions from existing ones. For example, we might use the quadratic above for the first half of the element's movement, and *f(t) = (t-1)^2 + 1* for the second half (so the element deccelerates towards its final position.)

T1. Implement a few tweening functions. This shouldn't take you very long.

[penner]: http://robertpenner.com/easing

[^unboxing]: The more perfomrance oriented of your might object to the additional indirection introduced by the `Easing` wrapper. We can remove it in many cases by using a *value type*. This goes beyond Essential Scala, but it is a fairly simple thing to implement: simply extend `AnyVal`.
