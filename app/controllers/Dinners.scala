package controllers

import play.api.mvc._
import models.Dinner
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import anorm.NotAssigned
import java.text.{SimpleDateFormat, DateFormat}
import util.matching.Regex

/**
 * @author mats, 2012-11-26
 */
object Dinners extends Controller {

  val datePattern = {
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    df match {
      case sdf: SimpleDateFormat => sdf.toPattern
      case _ => "yyyy-MM-dd HH:mm"
    }
  }

  val dinnerForm = Form(
    mapping (
      "title" -> nonEmptyText.verifying(error => "Title required"),
      "eventDate" -> date(datePattern),
      "description" -> nonEmptyText,
      "address" -> nonEmptyText,
      "country" -> nonEmptyText,
      "contactPhone" -> nonEmptyText,
      "latitude" -> of(Global.doubleFormat),
      "longitude" -> of(Global.doubleFormat)
    )(
      (title, eventDate, description, address, country, contactPhone, latitude, longitude) =>
        Dinner(NotAssigned, title, eventDate, description, "", contactPhone, address, country, latitude, longitude)
     )
     ((dinner: Dinner) => Option(dinner.title, dinner.eventDate, dinner.description, dinner.address,
                                 dinner.country, dinner.contactPhone, dinner.latitude, dinner.longitude)
     ).verifying("Phone# does not match country", x => PhoneValidator.isValidNumber(x.contactPhone, x.country))
  )

  def index = Action {
    val dinners = Dinner.findUpcomingDinners()
    Ok(views.html.Dinners.index(dinners))
  }

  def details(id: Long) = Action {
    Dinner.findById(id).map { dinner =>
      Ok(views.html.Dinners.details(id, dinner))
    }.getOrElse(Ok(views.html.Dinners.notFound()))
  }

  def edit(id: Long) = Action {
    Dinner.findById(id).map { dinner =>
      Ok(views.html.Dinners.edit(id, dinnerForm.fill(dinner)))
    }.getOrElse(Ok(views.html.Dinners.notFound()))
  }

  def update(id: Long) = Action { implicit request =>
    dinnerForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.Dinners.edit(id, formWithErrors)),
      dinner => {
        Dinner.update(id, dinner)
        Ok(views.html.Dinners.details(id, dinner))
      }
    )
  }
}

object PhoneValidator {

  val countryRegex = Map(
    "USA" -> """^[2-9]\d{2}-\d{3}-\d{4}$""".r,
    "UK" -> """(^1300\d{6}$)|(^1800|1900|1902\d{6}$)|(^0[2|3|7|8]{1}[0-9]{8}$)|(^13\d{4}$)|(^04\d{2,3}\d{6}$)""".r,
    "Netherlands" -> """(^\+[0-9]{2}|^\+[0-9]{2}\(0\)|^\(\+[0-9]{2}\)\(0\)|^00[0-9]{2}|^0)([0-9]{9}$|[0-9\-\s]{10}$)""".r
  )

  def isValidNumber(phoneNumber: String, country: String): Boolean = {
    countryRegex.get(country) match {
      case None => false
      case Some(regex) => {
        regex findFirstIn phoneNumber match {
          case Some(_) => true
          case None => false
        }
      }
    }
  }

  val countries = countryRegex.keys
}
