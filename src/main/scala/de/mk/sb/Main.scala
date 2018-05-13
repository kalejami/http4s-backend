package de.mk.sb

import java.util.UUID

import cats.effect.IO
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s.server.blaze._
import org.http4s.server.middleware._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._
import io.circe.generic.auto._
import org.http4s.circe._

import scala.concurrent.ExecutionContext.Implicits.global

final case class Comment(userId: String, id: String, title: String, body: String)

object Main extends StreamApp[IO] {

  val helloWorldService: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "comments" / name =>
      val id = UUID.randomUUID().toString
      Ok(List(Comment(id, id, name, name)).asJson)
  }

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(CORS(helloWorldService), "/")
      .serve
}
