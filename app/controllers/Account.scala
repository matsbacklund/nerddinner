package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import play.api.data.validation.Constraints._

/**
 * @author backlmat, 2012-11-26
 */
case class User(username: String, email: String, password: String)

object Account extends Controller {

  val minPasswordLength = 6

  val registrationForm = Form(
    mapping (
      "username" -> text.verifying(pattern("""[0-9.+]+""".r, error="A valid phone number is required")),
      "email" -> nonEmptyText,
      "password" -> tuple(
        "main" -> nonEmptyText(minLength = minPasswordLength),
        "confirm" -> nonEmptyText
      ).verifying(
        "The password and confirmation password do not match.", password => password._1 == password._2
      )
    )((username, email, password) => User(username, email, password._1))((user: User) => Option(user.username, user.email, ("", "")))
  )

  def logon = Action {
    Ok(views.html.Account.logon())
  }

  def index = Action {
    Ok(views.html.Account.register(registrationForm, minPasswordLength))
  }

  def register = Action { implicit request =>
    registrationForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.Account.register(formWithErrors)),
      value => Redirect(routes.Home.index())
    )
  }
}
