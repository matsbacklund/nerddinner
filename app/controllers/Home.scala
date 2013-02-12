package controllers

import play.api._
import play.api.mvc._

/**
 * @author backlmat, 2012-11-26
 */
object Home extends Controller with Secured {

  def index = Action { implicit request =>
    Ok(views.html.Home.index("Welcome to Play!"))
  }

  def about = Action { implicit request =>
    Ok(views.html.Home.about())
  }

  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        Rsvps.register
      )
    ).as(JAVASCRIPT)
  }
}