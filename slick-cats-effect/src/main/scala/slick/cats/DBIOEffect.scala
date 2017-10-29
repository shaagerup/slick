package slick.cats
import scala.concurrent._
import scala.util._

import slick.basic.BasicBackend
import slick.dbio.DBIO

import cats.effect.IO
import cats.Eval.now


class DBIOEffect(db : BasicBackend#DatabaseDef)(implicit ec: ExecutionContext) extends cats.effect.Effect[DBIO] {

  override def runAsync[A](fa: DBIO[A])(cb: (Either[Throwable, A]) => IO[Unit]): IO[Unit] = {
     IO.fromFuture( 
       now( 
         db.run( fa )
            .map(v => cb(Right(v)))
            .recover{ case e => cb(Left(e)) }
      )
    ).flatMap(identity)
  }

  override def async[A](k: ((Either[Throwable, A]) => Unit) => Unit): DBIO[A] = {
    val p = Promise[A]
    k { res : Either[Throwable, A] => res match {
        case Left(e) => p.failure(e)
        case Right(v) => p.success(v)
    } }
    DBIO.from(p.future)
  }

  override def suspend[A](thunk: => DBIO[A]): DBIO[A] = DBIO.suspend(thunk)

  override def flatMap[A, B](fa: DBIO[A])(f: (A) => DBIO[B]): DBIO[B] = fa.flatMap(f)

  override def tailRecM[A, B](a: A)(f: A => DBIO[Either[A, B]]): DBIO[B] =
        f(a).flatMap {
          case Left(a1) => tailRecM(a1)(f)
          case Right(b) => DBIO.successful(b)
        }

  override def raiseError[A](e: Throwable): DBIO[A] = DBIO.failed(e)

  override def handleErrorWith[A](fa: DBIO[A])(f: (Throwable) => DBIO[A]): DBIO[A] =
    fa.asTry.flatMap {
      case Success(a) => DBIO.successful(a)
      case Failure(t) => f(t)
    }
  override def pure[A](x: A): DBIO[A] = DBIO.successful(x)

}