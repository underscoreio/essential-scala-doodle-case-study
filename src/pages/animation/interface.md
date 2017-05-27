## Interface

We can start sketching an interface. Let's call our type `Stream[A]`, where the type variable indicates the type of elements that the event stream produces.

Write down an interface that captures the examples we've seen so far. Are there any other methods you think we should include?

<div class="solution">
This is a sufficient interface:

```scala
sealed trait Stream[A] {
  def map[B](f: A => B): Stream[B]

  def scanLeft[B](seed: B)(f: (B,A) => B): Stream[B]

  def join[B](that: Stream[B]): EventStream[(A,B)]
  
  def runFold[B](zero: B)(f: (B, A) => B): B
}
```

You might be tempted to add `flatMap`.

```scala
def flatMap[B](f: A => Stream[B]): EventStream[B]
```


Let's think for a minute about what this means. The function `f` uses the input `A` to choose an `Stream[B]` to handle further processing, thereby possibly changing the downstream event processing network on every input. There are event stream systems that allow this but it is tricky to reason about. For these reasons we've chosen to not implement `flatMap` but we encourage you to implement it if you want an additional challenge.

By the way, `scanLeft` is sometimes called `foldP`, meaning "fold over the past". This is the name you'll find in the "functional reactive programming" literature.
</div>

The `Stream` interface is simpler than the `Image` interface, though we have new tools (type variables, functions) that we're using. The implementation, however, is more complex and there is much more variation in possible implementations. In the next section we'll look at one possible implementation, but we encourage you to explore your own ideas.
