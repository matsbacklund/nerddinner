package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.User

/**
 * @author backlmat, 2012-11-26
 */
object Account extends Controller {

  val minPasswordLength = 6

  val logonForm = Form(
    mapping (
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    )(
      (username, password, _) => User(username, "", password)
    )(
      (user: User) => Option(user.username, "", false)
    ).verifying("The user name or password provided is incorrect.", result => result match {
      case User(username, _, password, _) => User.authenticate(username, password)
    })
  )

  val registrationForm = Form(
    mapping (
      "username" -> nonEmptyText,
      "email" -> email,
      "password" -> tuple(
        "main" -> nonEmptyText(minLength = minPasswordLength),
        "confirm" -> nonEmptyText
      ).verifying(
        "The password and confirmation password do not match.", password => password._1 == password._2
      )
    )(
      (username, email, password) => User(username, email, password._1)
    )(
      (user: User) => Option(user.username, user.email, ("", ""))
    )
  )

  def logon = Action {
    Ok(views.html.Account.logon(logonForm))
  }

  def logoff = Action {
    Redirect(routes.Home.index()).withNewSession
  }

  def authenticate = Action { implicit request =>
    logonForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.Account.logon(formWithErrors)),
      user => Redirect(routes.Home.index()).withSession(Security.username -> user.username)
    )
  }

  def registration = Action { implicit request =>
    Ok(views.html.Account.register(registrationForm, minPasswordLength))
  }

  def register = Action { implicit request =>
    registrationForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.Account.register(formWithErrors)),
      user => {
        User.create(user)
        Redirect(routes.Account.logon())
      }
    )
  }
}

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Account.logon())

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }

  def IsAuthenticatedUser(f: User => Request[AnyContent] => Result) = IsAuthenticated { username => request =>
    User.findByName(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  implicit def user(implicit request: RequestHeader): Option[User] = {
    username(request).map { username =>
      User.findByName(username)
    }.getOrElse(None)
  }
}
