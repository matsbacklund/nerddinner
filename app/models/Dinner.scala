package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer

/**
 * @author maba, 2013-08-15
 */
case class Dinner(
                   id: Pk[Long],
                   title: String,
                   eventDate: Date,
                   description: String,
                   hostedBy: String,
                   contactPhone: String,
                   address: String,
                   country: String,
                   latitude: Double,
                   longitude: Double
                   ) {

  def isValid = {
    ruleViolations.isEmpty
  }

  def ruleViolations = {
    val violations = new ListBuffer[RuleViolation]
    if (title == null || title.isEmpty) {
      violations += RuleViolation("Title Required", "title")
    }
    if (description == null || description.isEmpty) {
      violations += RuleViolation("Description Required", "description")
    }
    if (hostedBy == null || hostedBy.isEmpty) {
      violations += RuleViolation("Hosted By Required", "hostedBy")
    }
    if (address == null || address.isEmpty) {
      violations += RuleViolation("Address Required", "address")
    }
    if (country == null || country.isEmpty) {
      violations += RuleViolation("Country Required", "country")
    }
    if (contactPhone == null || contactPhone.isEmpty) {
      violations += RuleViolation("Contact Phone Required", "contactPhone")
    }
    if (!PhoneValidator.isValidNumber(contactPhone, country)) {
      violations += RuleViolation("Phone# does not match country", "contactPhone")
    }
    violations.toList
  }
}

object Dinner {
  val parser = {
    get[Pk[Long]]("id") ~
    get[String]("title") ~
    get[Date]("event_date") ~
    get[String]("description") ~
    get[String]("hosted_by") ~
    get[String]("contact_phone") ~
    get[String]("address") ~
    get[String]("country") ~
    get[Double]("latitude") ~
    get[Double]("longitude") map {
      case id~title~eventDate~description~hostedBy~contactPhone~address~country~latitude~longitude => {
        Dinner(id, title, eventDate, description, hostedBy, contactPhone, address, country, latitude, longitude)
      }
    }
  }

  //
  // Query Methods

  def findAll(): Seq[Dinner] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT * FROM dinners
        """
      ).as(
        Dinner.parser *
      )
    }
  }

  def findUpcoming(): Seq[Dinner] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT * FROM dinners
          WHERE event_date > {now}
          ORDER BY event_date
        """
      ).on(
        'now -> new Date()
      ).as(
        Dinner.parser *
      )
    }
  }

  def findById(id: Long): Option[Dinner] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT * FROM dinners
          WHERE id = {id}
        """
      ).on(
        'id -> id
      ).as(
        Dinner.parser.singleOpt
      )
    }
  }

  //
  // Insert/Delete Methods

  def create(dinner: Dinner): Dinner = {
    DB.withConnection { implicit connection =>
      val id: Option[Long] = SQL(
        """
          INSERT INTO dinners(title, event_date, description, hosted_by, contact_phone, address, country, latitude, longitude)
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

      dinner.copy(id = Id(id.get))
    }
  }

  def delete(dinner: Dinner) {
    DB.withTransaction { implicit connection =>
      SQL(
        """
          DELETE FROM rsvps
          WHERE dinner_id = {id}
        """
      ).on(
        'id -> dinner.id.get
      ).executeUpdate()

      SQL(
        """
          DELETE FROM dinners
          WHERE id = {id}
        """
      ).on(
        'id -> dinner.id.get
      ).executeUpdate()
    }
  }

  def update(dinner: Dinner) {
    DB.withConnection { implicit connection =>
      SQL(
        """
          UPDATE dinners
          SET title = {title}, event_date = {eventDate}, description = {description}, hosted_by = {hostedBy},
              contact_phone = {contactPhone}, address = {address}, country = {country}, latitude = {latitude}, longitude = {longitude}
          WHERE id = {id}
        """
      ).on(
        'id -> dinner.id.get,
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
}