import scala.language.higherKinds

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Monad[F[_]] extends Functor[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def point[A](a: A): F[A]
}

trait Applicative[F[_]] extends Functor[F] {
  def zip[A, B](fa: F[A])(fb: F[B]): F[(A, B)]
  def point[A](a: A): F[A]
}

object ListInstances {
  implicit object list extends Functor[List] with Monad[List] with Applicative[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] =
      fa.map(f)
    def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] =
      fa.flatMap(f)
    def point[A](a: A): List[A] =
      List(a)
    def zip[A, B](fa: List[A])(fb: List[B]): List[(A, B)] =
      fa.zip(fb)
  }
}

object EventStreamInstances {
  implicit object eventStream extends Functor[EventStream] with Monad[EventStream] with Applicative[EventStream] {
    def map[A, B](fa: EventStream[A])(f: A => B): EventStream[B] =
      fa.map(f)
    def flatMap[A, B](fa: EventStream[A])(f: A => EventStream[B]): EventStream[B] =
      fa.flatMap(f)
    def point[A](a: A): EventStream[A] =
      EventStream.now(a)
    def zip[A, B](fa: EventStream[A])(fb: EventStream[B]): EventStream[(A, B)] =
      fa.zip(fb)
  }
}

object IdInstances {
  type Id[A] = A

  implicit object list extends Functor[Id] with Monad[Id] with Applicative[Id] {
    def map[A, B](fa: Id[A])(f: A => B): Id[B] =
      f(fa)
    def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] =
      f(fa)
    def point[A](a: A): Id[A] =
      a
    def zip[A, B](fa: Id[A])(fb: Id[B]): Id[(A, B)] =
      (fa, fb)
  }
}
