package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.util.Date

/**
 * @author mats, 2012-11-26
 */
case class Dinner(id: Pk[Long], title: String, eventDate: Date, description: String, hostedBy: String)

object Dinner {

  val dinner = {
    get[Pk[Long]]("id") ~
    get[String]("title") ~
    get[Date]("event_date") ~
    get[String]("description") ~
    get[String]("hosted_by") map {
      case id~title~eventDate~description~hostedBy => Dinner(id, title, eventDate, description, hostedBy)
    }
  }

  def findAllDinners(): Seq[Dinner] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM DINNER").as(dinner *)
    }
  }

  def findById(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM DINNER WHERE id={id}").on('id -> id).as(dinner.singleOpt)
    }
  }
}
