package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.util.Date

/**
 * @author mats, 2012-11-26
 */
case class Dinner(id: Pk[Long], title: String, eventDate: Date, description: String, hostedBy: String,
                  contactPhone: String, address: String, country: String, latitude: Double, longitude: Double)

object Dinner {

  val simple = {
    get[Pk[Long]]("DinnerId") ~
    get[String]("Title") ~
    get[Date]("EventDate") ~
    get[String]("Description") ~
    get[String]("HostedBy") ~
    get[String]("ContactPhone") ~
    get[String]("Address") ~
    get[String]("Country") ~
    get[Double]("Latitude") ~
    get[Double]("Longitude") map {
      case id~title~eventDate~description~hostedBy~contactPhone~address~country~latitude~longitude =>
        Dinner(id, title, eventDate, description, hostedBy, contactPhone, address, country, latitude, longitude)
    }
  }

  def findAllDinners(): Seq[Dinner] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM DINNER").as(Dinner.simple *)
    }
  }

  def findUpcomingDinners(): Seq[Dinner] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM Dinner WHERE EventDate > {now} ORDER BY EventDate").on('now -> new Date()).as(Dinner.simple *)
    }
  }

  def listUpcomingDinners(page: Int = 0, pageSize: Int = 10): Page[Dinner] = {
    val offset = pageSize * (page)
    val now = new Date()
    DB.withConnection { implicit connection =>
      val dinners = SQL(
        """
          SELECT * FROM Dinner
          WHERE EventDate > {now}
          ORDER BY EventDate
          LIMIT {pageSize} OFFSET {offset}
        """
      ).on(
        'now -> now,
        'pageSize -> pageSize,
        'offset -> offset
      ).as(
        Dinner.simple *
      )

      val totalRows = SQL(
        """
          SELECT COUNT(*) FROM Dinner
          WHERE EventDate > {now}
        """
      ).on(
        'now -> now
      ).as(scalar[Long].single)

      Page(dinners, page, offset, totalRows)
    }
  }

  def findById(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM Dinner WHERE DinnerId={id}").on('id -> id).as(Dinner.simple.singleOpt)
    }
  }

  def add(dinner: Dinner): Dinner = {
    DB.withTransaction { implicit connection =>
      val id: Option[Long] = SQL("""INSERT INTO Dinner(Title, EventDate, Description, HostedBy, ContactPhone, Address, Country, Latitude, Longitude)
                 VALUES({title}, {eventDate}, {description}, {hostedBy}, {contactPhone}, {address}, {country}, {latitude}, {longitude})
          """
      ).on(
        'title -> dinner.title,
        'eventDate -> dinner.eventDate,
        'description -> dinner.description,
        'hostedBy -> dinner.hostedBy,
        'contactPhone -> dinner.contactPhone,
        'address -> dinner.address,
        'country -> dinner.country,
        'latitude -> dinner.latitude,
        'longitude -> dinner.longitude
      ).executeInsert()

      SQL(
        """
          INSERT INTO Rsvp(DinnerId, AttendeeName) VALUES({dinnerId}, {attendeeName})
        """
      ).on(
        'dinnerId -> id.get,
        'attendeeName -> dinner.hostedBy
      ).executeUpdate()

      dinner.copy(id = Id(id.get))
    }
  }

  def delete(dinner: Dinner) {
    delete(dinner.id.get)
  }

  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM Dinner WHERE DinnerId = {id}").on('id -> id).executeUpdate()
    }
  }

  def update(id: Long, dinner: Dinner) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          UPDATE Dinner
          SET Title = {title}, EventDate = {eventDate}, Description = {description}, HostedBy = {hostedBy}, ContactPhone = {contactPhone},
              Address = {address}, Country = {country}, Latitude = {latitude}, Longitude = {longitude}
          WHERE DinnerId = {id}
        """
      ).on(
        'id -> id,
        'title -> dinner.title,
        'eventDate -> dinner.eventDate,
        'description -> dinner.description,
        'hostedBy -> dinner.hostedBy,
        'contactPhone -> dinner.contactPhone,
        'address -> dinner.address,
        'country -> dinner.country,
        'latitude -> dinner.latitude,
        'longitude -> dinner.longitude
      ).executeUpdate()
    }
  }

  def addUser(dinnerId: Long, username: String) {
    DB.withConnection { implicit connection =>
      SQL("INSERT INTO Rsvp(DinnerId, AttendeeName) VALUES({dinnerId}, {username})").on(
        'dinnerId -> dinnerId,
        'username -> username
      ).executeUpdate()
    }
  }

  def isHostedBy(dinnerId: Long, username: String): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT COUNT(dinner.hostedBy) = 1 from Dinner dinner
          WHERE dinner.DinnerId = {dinnerId} and dinner.hostedBy = {username}
        """
      ).on(
        'dinnerId -> dinnerId,
        'username -> username
      ).as(
        scalar[Boolean].single
      )
    }
  }

  def isUserRegistered(dinnerId: Long, username: String): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT COUNT(rsvp.AttendeeName) = 1 from Rsvp rsvp
          JOIN Dinner dinner ON dinner.DinnerId = rsvp.DinnerId
          WHERE dinner.DinnerId = {dinnerId} and rsvp.AttendeeName = {username}
        """
      ).on(
        'dinnerId -> dinnerId,
        'username -> username
      ).as(
        scalar[Boolean].single
      )
    }
  }
}
