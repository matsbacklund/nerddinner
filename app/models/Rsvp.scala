package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

/**
 * @author backlmat, 2012-11-27
 */
case class Rsvp(id: Pk[Long], dinner: Dinner, attendeeName: String)

object Rsvp {

  val rsvp = {
    get[Pk[Long]]("RsvpId") ~
    get[Long]("DinnerId") ~
    get[String]("AttendeeName") map {
      case id~dinnerId~attendeeName =>
        Rsvp(id, Dinner.findById(dinnerId).get, attendeeName)
    }
  }

  def delete(rsvp: Rsvp) {
    delete(rsvp.id.get)
  }

  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM Rsvp WHERE RsvpId = {id}").on('id -> id).executeUpdate()
    }
  }
}
