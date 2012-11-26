package controllers

import play.api.mvc.{Action, Controller}

/**
 * @author backlmat, 2012-11-26
 */
object Home  extends Controller {

  def index = Action {
    Ok(views.html.Home.index("Welcome to ASP.NET MVC!"))
  }

  def about = Action {
    Ok(views.html.Home.about())
  }
}