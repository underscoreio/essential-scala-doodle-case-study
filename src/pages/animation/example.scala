// Framework to run the code
final case class Vec(x: Double, y: Double) {
  def +(that: Vec): Vec =
    Vec(this.x + that.x, this.y + that.y)
}


// User input is a Key
sealed trait Key
final case object Up extends Key
final case object Down extends Key
final case object Left extends Key
final case object Right extends Key

// Velocity is represented as a two dimensional Vector
def currentVelocity(previousVelocity: Vec, input: Key): Vec =
  input match {
    case Up => previousVelocity + Vec(0, 1)
    case Down => previousVelocity + Vec(0, -1)
    case Left => previousVelocity + Vec(-1, 0)
    case Right => previousVelocity + Vec(1, 0)
  }

// Location is represented as a two dimensional Vector, by abuse of notation
def currentLocation(previousLocation: Vec, velocity: Vec): Vec =
  previousLocation + velocity

val input = List(Up, Up, Down, Down, Left, Right, Left, Right)

val images: List[Vec] =
  input.scanLeft(Vec(0, 0)){ currentVelocity }.
    scanLeft(Vec(0, 0)){ currentLocation }
