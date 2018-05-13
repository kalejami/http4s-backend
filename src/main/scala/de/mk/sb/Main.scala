package de.mk.sb

import java.util.UUID

import cats.effect.IO
import doobie._
import doobie.free.connection
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s.server.blaze._
import org.http4s.server.middleware._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._
import io.circe.generic.auto._
import org.flywaydb.core.Flyway
import org.http4s.circe._

import scala.concurrent.ExecutionContext.Implicits.global

final case class Comment(userId: String, id: String, title: String, body: String)

object Main extends StreamApp[IO] {

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.h2.Driver", "jdbc:h2:mem:test", "sa", "sa"
  )

  val migrate: ConnectionIO[Int] = connection.delay {
    val flyway = new Flyway()
    flyway.setDataSource("jdbc:h2:mem:test", "sa", "sa")
    flyway.migrate()
  }

  val uuid: ConnectionIO[UUID] = connection.delay(UUID.randomUUID())

  def byTitle(title: String): Query0[Comment] = sql"SELECT * FROM COMMENT WHERE TITLE=$title".query
  def insert(comment: Comment): Update0 = sql"INSERT INTO COMMENT VALUES(${comment.userId}, ${comment.id}, ${comment.title}, ${comment.body})".update

  val helloWorldService: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "comments" / name =>
      val p = for {
        _ <- migrate
        id1 <- uuid
        _ <- insert(Comment(id1.toString, id1.toString, name, id1.toString)).run
        id2 <- uuid
        _ <- insert(Comment(id2.toString, id2.toString, name, id2.toString)).run
        comments <- byTitle(name).to[List]
      } yield comments
      Ok(p.transact(xa).map(_.asJson))
  }

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(CORS(helloWorldService), "/")
      .serve
}
