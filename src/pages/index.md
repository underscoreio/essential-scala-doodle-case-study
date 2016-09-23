# Introduction

This supplement to Essential Scala brings together the book's main concepts in a sizable case study. The goal is to develop a *two-dimensional vector graphics and animation* library similar to [Doodle][doodle]. Let us first describe the what vector graphics are, then why this makes a good case study, and finally describe the structure of the project.

By vector graphics we mean images that are described in terms of lines and curves, rather than in terms of pixels. Vector graphics are independent of how they are drawn. They can be rendered equally well on a high resolution plotter or a tiny mobile screen. They can be arbitrarily transformed without losing information, unlike pixel graphics which distort when they are rotated, for example. This structure is something our library will heavily leverage.

So why a case study vector graphics? There are a few reasons. Firstly, graphics are fun, and hopefully something you can enjoy even if you haven't worked with computer graphics before. Having a tangible output is of great benefit. We can directly draw on the screen the concepts we're working with! Finally, we'll see that graphics great vehicle for the concepts taught in Essential Scala, and we can wrap up most of the content in the course in this one case study.

The case study is divided into three main sections:

1. Building the basic objects and methods for our library, which makes heavy use of algebraic data types and structural recursion.
2. Adding animations, which introduces sequencing abstractions such as `map` and `fold`.
3. Abstracting the rendering pipeline, which introduces type classes and touches briefly on some more advanced functional programming concepts.

Each section asks you to implement part of the library. Supplementing this supplement is a code repository containing support code as well as working implementations for each exercise. There are many possible implementations for each exercise, each making different design tradeoffs. Our solution is just one of these, and you shouldn't take it to be any more correct than other solutions you may come up with. A primary goal of this supplement is to understand the tradeoffs different solutions make.

To get the most from this case study *you have to do the work*. While you will get some value from reading through and looking at our solutions, you will get infinitely more if you attempt every exercise yourself first.

Finally, we always enjoy talking to other programmers. If you have any comments or questions about this case study, do drop us an email at `noel@underscore.io` and `dave@underscore.io`.
