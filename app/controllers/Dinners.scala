package controllers

import play.api.mvc._
import models.Dinner
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import java.text.{ParsePosition, FieldPosition, SimpleDateFormat, DateFormat}
import java.util.{Calendar, Date}

/**
 * @author mats, 2012-11-26
 */
object Dinners extends Controller with Secured {

  val datePattern = {
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    df match {
      case sdf: SimpleDateFormat => sdf.toPattern
      case _ => "yyyy-MM-dd HH:mm"
    }
  }

  val dinnerForm = Form(
    mapping (
      "title" -> text.verifying("Title required", {!_.isEmpty}),
      "eventDate" -> date(datePattern),
      "description" -> text.verifying("Description required", {!_.isEmpty}),
      "address" -> text.verifying("Address required", {!_.isEmpty}),
      "country" -> text.verifying("Country required", {!_.isEmpty}),
      "contactPhone" -> text.verifying("Phone# required", {!_.isEmpty}),
      "latitude" -> of(Global.doubleFormat),
      "longitude" -> of(Global.doubleFormat)
    )(
      (title, eventDate, description, address, country, contactPhone, latitude, longitude) =>
        Dinner(NotAssigned, title, eventDate, description, "", contactPhone, address, country, latitude, longitude)
     )
     (
       (dinner: Dinner) =>
         Option(dinner.title, dinner.eventDate, dinner.description, dinner.address,
                dinner.country, dinner.contactPhone, dinner.latitude, dinner.longitude)
     ).verifying("Phone# does not match country", x => PhoneValidator.isValidNumber(x.contactPhone, x.country))
  )

  /**
   * Handle default path requests, redirect to dinners list
   */
  def index = Action { implicit request =>
    Redirect(routes.Dinners.page(0))
  }

  def page(page: Int) = Action { implicit request =>
    Ok(views.html.Dinners.index(Dinner.listUpcomingDinners(page)))
  }

  def details(id: Long) = Action { implicit request =>
    Dinner.findById(id).map { dinner =>
      Ok(views.html.Dinners.details(id, dinner))
    }.getOrElse(Ok(views.html.Dinners.notFound()))
  }

  def edit(id: Long) = IsAuthenticated { username => implicit request =>
    Dinner.findById(id).map { dinner =>
      Dinner.isHostedBy(id, username) match {
        case true => {
          Ok(views.html.Dinners.edit(id, dinnerForm.fill(dinner), countries))
        }
        case false => {
          Ok(views.html.Dinners.invalidOwner())
        }
      }
    }.getOrElse(Ok(views.html.Dinners.notFound()))
  }

  def update(id: Long) = IsAuthenticated { username => implicit request =>
    Dinner.isHostedBy(id, username) match {
      case true => {
        dinnerForm.bindFromRequest().fold(
          formWithErrors => BadRequest(views.html.Dinners.edit(id, formWithErrors, countries)),
          dinner => {
            Dinner.update(id, dinner)
            Ok(views.html.Dinners.details(id, dinner))
          }
        )
      }
      case false => {
        Ok(views.html.Dinners.invalidOwner())
      }
    }
  }

  def create = IsAuthenticated { _ => implicit request =>
    val cal = Calendar.getInstance()
    cal.setTime(new Date())
    cal.add(Calendar.DATE, 7)
    val date = cal.getTime

    val dinner = Dinner(NotAssigned, "", date, "", "", "", "", "", 0, 0)
    Ok(views.html.Dinners.create(dinnerForm.fill(dinner), countries))
  }

  def insert = IsAuthenticated { username => implicit request =>
    dinnerForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.Dinners.create(formWithErrors, countries)),
      dinner => {
        val dinnerWithHost = dinner.copy(hostedBy = username)
        val d = Dinner.add(dinnerWithHost)
        Redirect(routes.Dinners.details(d.id.get))
      }
    )
  }

  def delete(id: Long) = Action { implicit request =>
    Dinner.findById(id).map { dinner =>
      Ok(views.html.Dinners.delete(id, dinner))
    }.getOrElse(Ok(views.html.Dinners.notFound()))
  }

  def remove(id: Long) = Action { implicit request =>
    Dinner.findById(id).map { dinner =>
      Dinner.delete(dinner)
      Ok(views.html.Dinners.deleted())
    }.getOrElse(Ok(views.html.Dinners.notFound()))
  }

  val countries = PhoneValidator.countries.toList
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
