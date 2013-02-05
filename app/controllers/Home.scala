package controllers

import play.api.mvc.{Action, Controller}

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
}