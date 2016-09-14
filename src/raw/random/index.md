# Random Art

In this section we'll develop a library for generating random data. Why random data? In our context, it is useful for generating art, as depicted in [@fig:random:brownian-motion]. There are other uses, including

- generating test data for *property based testing*; and
- performing probabilistic inference on data.


![Brownian motion, studies of which led to Jean Perrin receiving the Nobel Prize in 1926, rendered for artistic effect.](src/raw/random/brownian-motion.pdf+svg){#fig:random:brownian-motion}


## Why Not `scala.util.Random`?

The most straightforward way to generate random values in Scala is to use `scala.util.Random`. Here are some exampes

```tut:book
val rng = scala.util.Random
rng.nextDouble()
rng.nextDouble()
```

Why would we not use to generate random data? What principles does it break?

<div class="solution">
The methods on `scala.util.Random` return different values each time we call them. This means we cannot use substitution to reason about them. Concretely.

```tut:book
val answer = scala.util.Random.nextDouble()
answer + answer
```

is not the same as 

```tut:book
scala.util.Random.nextDouble() + scala.util.Random.nextDouble()
```

Once we lose substitution we lose other desirable properties like composition.
</div>

## A Plan

Our solution to this problem is to do exactly what we did with `Image`: separate describing and running the computation. For `Image` this means we separate constructing the image (with `above`, `beside`, and so on) and drawing it on the screen. For our random data problem we will separate describing how to construct the random data, and actually randomly generating it.
