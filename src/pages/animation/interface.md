## Interface

We can start sketching an interface. Let's call our type `EventStream[A]`, where the type variable indicates the type of elements that the event stream produces.

Write down an interface that captures the examples we've seen so far. Are there any other methods you think we should include?

<div class="solution">
This is a sufficient interface:

```scala
sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B]

  def scanLeft[B](seed: B)(f: (A, B) => B): EventStream[B]

  def join[B](that: EventStream[B]): EventStream[(A,B)]
}
```

You might be tempted to add `flatMap`. Let's think for a minute about what means. Essentialy `flatMap` allows the function passed to it to choose the upstream event processing network that handles each input. There are event stream systems that allow this but it is tricky to implement and even trickier to reason about. For these reasons I've chosen to not implement `flatMap`.

By the way, `scanLeft` is sometimes called `foldP`, meaning "fold over the past". This is the name you'll find in the "functional reactive programming" literature.
</div>

Compared to the `Image` interface, there is less going on in the `EventStream` interface, though we have new tools (type variables, functions) that we've incorporated. The implementation, however, is going to require a bit more work. We turn to this next.
