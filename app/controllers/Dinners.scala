package controllers

import play.api.mvc._
import play.api.templates.Html
import models.Dinner

/**
 * @author maba, 2013-08-15
 */
object Dinners extends Controller {

  def list = Action {
    val dinners = Dinner.findUpcoming()
    Ok(Html("<h1>Coming Soon: Dinners</h1>"))
  }

  def details(id: Long) = Action {
    Dinner.findById(id).map { dinner =>
      Ok(views.html.Dinners.details(dinner))
    }.getOrElse {
      Ok(views.html.Dinners.notFound())
    }
  }
}
