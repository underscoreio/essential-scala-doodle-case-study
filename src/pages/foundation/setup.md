## Getting Setup

The code we will build on can be found on [Github](https://github.com/underscoreio/doodle-case-study). Fork this repository so you have your own copy to make changes in. (If you're not experienced with Git and Github, it's a good idea to go through Github's documentation to get a better understanding of how it all works.)

Now run `sbt`, Scala's standard build tool. If you don't have it installed already, you can run `sbt.sh` (Mac or Linux) or `sbt.bat` (Windows) as appropriate. You should see output like the below and arrive at a Scala prompt.

```bash
Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=512m; support was removed in 8.0

[info] Loading global plugins from /Users/noel/.sbt/0.13/plugins
[info] Loading project definition from /Users/noel/dev/essential-scala/case-study-code/project
[info] Set current project to doodle-case-study (in build file:/Users/noel/dev/essential-scala/case-study-code/)
> 
```

From this prompt we can give SBT commands. A good start is to enter `compile`, which compiles all the code we've already provided. This should do some work and finish without issue.

A more interesting command is `run`. Try this now. A window with an interesting picture should pop up. Close the window and you should return to the SBT prompt.

Our final exploration of SBT is to run the `console` command. This gets us to a Scala console where we can run Scala code directly. We can try exciting programs like `1 + 1`, but more interesting is to try

```scala
val canvas = Java2DCanvas.canvas
Spiral.draw(canvas)
```

This should pop up a window containing the same picture we saw before.

We've done enough exploration of the code for now. Let's head back to the problem we're trying to solve. We'll be back in the code soon enough.
