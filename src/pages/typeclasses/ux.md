## Improving User Experience with Implicits

In this section we'll look at a few improvements we can make to the `Image` API using implicits.

### Implicit Parameters

It's a bit inconvenient to always explicitly pass a `Canvas` to the `draw` method on `Image`. Let's add some implicit magic to make the `Canvas` optional. Make the `Canvas` argument of `draw` an implicit parameter, and makes corresponding changes to the canvas implementations to make implicit values available. Hint: look in the objects `Java2DCanvas` and `HtmlCanvas`.


### Syntax

The interface for animating an `EventStream[Image]` is a bit clunky. We didn't add an `animate` method to `EventStream` because event streams work with generic types, though it feels like this is where such a method should live. We ended up creating a method `animate` on an object, passing it both an `EventStream[Image]` and a `Canvas`.

We can add `animate` as a method to only `EventStream[Image]` via the magic of implicit classes. In the package `doodle.syntax` (directory is `shared/src/main/scala/doodle/syntax`) add an implicit class called `EventStreamImageSyntax` following the conventions already in use in that package. Wire in your syntax to the package object in `package.scala` following the existing conventions.

When you `import doodle.syntax.eventStreamImage._` you should now have an `animate` method available on any object of type `EventStream[Image]`. This method should accept a `Canvas` as an implicit parameter, just as we have done with `draw`.
