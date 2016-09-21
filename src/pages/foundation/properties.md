## Properties of a Better System

Now we know what is wrong with the imperative approach, let's think about what kind of properties should hold in a better system. The focus here is not on the kinds of pictures we can draw---whether we can use gradient fills, for example---but on the process of constructing pictures. What should that be like? Can you come up with a short description or a few terms that describe desirable properties and what they mean in the context of a drawing library? Take some time to think about this before reading our answer.

<div class="solution">
A *compostional* library allows us to build larger pictures from smaller ones by composing them. How can we compose images? We have already talked about layout as one possibility. We could define a new image as the composition of two images beside one another. You can imagine other layout operations to arrange images vertically, or to stack them on top of one another, and so on. We could also compose images using geometric transformations such as rotations and shearing, or using styling such as fill color.

Compostionality implies there is no global state. There are many closely related terms that all boil down to removing state: maintaining *substitution*, enabling *local reasoning*, *referential transparency*, or *purity*.

*Closure* is another property implied by compositionality. This means there are operations that take two or more pictures and return an element of the same type. Closure allows us to apply operations indefinitely to build more complex pictures from simpler ones.

Our library should allow operations in terms of a *local coordinate system* to make composition easier.

Finally, we want an *expressive* library, a rather loosely defined term that we can take to mean we should write the minimal amount of code required to achieve our goal.
</div>
