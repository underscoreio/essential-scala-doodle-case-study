# Essential Scala Week Three Notes

Our mission this week is to go crazy with type classes and implicits. We are aiming to explore a variety of applications of implicits in Scala by making several improvements to Doodle.

## Reusable Animations

Lask week we developed a reactive event system, which we used to implement animations. Consider animating a UI element, such as a toolbar that slides out. We could simply linearly interpolate the position of the toolbar between the start and end points, but this simple movement lacks visual appeal. UI designers like to use movement that is more visually interesting. For example, our toolbar's movement might accelarate over time, and it might bounce when it reaches it's final destination.

Functions that interpoloate position over time are known as *tweening functions*. Commonly used tweening functions include quadratics and higher order polynomials, and damped spring models (to give bounce). A tweening functions usually accepts and returns a number between 0 and 1, indicating position between 0% and 100% of the final position. The more mathematically inclined will recognise this as the parametric form of a path. A quadratic tweening function can then be defined as *f(t) = t^2*, for *t in [0, 1]*. Given some basic functions we can compose new tweening functions from existing ones. For example, we might use the quadratic above for the first half of the element's movement, and *f(t) = (t-1)^2 + 1* for the second half (so the element deccelerates towards its final position.)

T1. Implement a few tweening functions. This shouldn't take you very long.


## Testing Animations

Testing tweening functions is fairly simple. There are two properties that should always hold:

- *f(0) = 0*
- *f(1) = 1*

As they are pure functions we can easily generate test data for them. What about testing *animations*? This is harder. To drive our `EventStreams` we need a source to generate events. We could create a dummy event stream that generate events as quickly as possible, but still have the issue of stopping the events at some point and inspecting the intermediate results. However, we should recognise that very little about a reactive program requires that is actually operates over an `EventStream`. Most of our code could be just as well written to use, say, `List`, and we can easily inspect the output of a sequence of `List` transforms.

Q1. `EventStream` and `List` don't have any common useful superclass, not should they. What tool should we use to express they have a common interface?

Q2. What name can we give to the common interface(s) that `EventStream` and `List` share?

T2. Implement these interfaces, and give implementations for `EventStream` and `List`. You will run into a problem doing this. Read on...

Kinds are like types for types. The describe the number of "holes" in a type. We distinguish between regular types that have no holes, and *type constructors* that have holes that we can fill to produce types.

`List` and `EventStream` both have a type parameter and so they are type constructors. They have a different kind to regular types. We must give them a type to create a concrete type like `List[Int]` or `EventStream[String]`. Type constructors are analogous to functions of a single parameter, operating on the type level rather than the value level. 

When we write a type parameter like `A` we must also tell Scala its kind. As you've probably guessed, not extra annotation means a regular type. To indicate a type constructor taking a single parameter we would write `F[_]`. `F` is the name of the type constructor, and `[_]` indicates it has a single parameter or hole.
 We must also enable *higher kinds* to use this feature of Scala.

Here is a very simple example of an identity function that expects a higher kinded type.

```
import scala.language.higherKinds

def higherIdentity[F[_], A](in: F[A]): F[A] = in
```

Q4: For extra bonus points implement these interfaces for normal types (i.e. for any type `A`). This is known as the identity monad / functor / applicative.

Q5: Why is this useful?


## Dependency Injection


## Enrichment

## Answers

T1: Easy.

Q1: A type class

Q2: `map` implies a `Functor`. `flatMap` implies a `Monad`. `join` implies an `Applicative` (an abstraction not discussed in Essential Scala; `zip` is the (almost) equivalent function on `List`). Note that we treat these abstractions very informally in Essential Scala. There are also laws or identities that instances of these abstractions should fulfil. Furthermore, `Monads` and `Applicatives` have a function `point(a: A): F[A]`. Finally, this treatment of `Applicative` is slightly different to the usual one in the literature.

Q3: TBC.
