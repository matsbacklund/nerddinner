package controllers

import play.api.mvc._
import models.Dinner

/**
 * @author mats, 2012-11-26
 */
object Dinners extends Controller {

  def index = Action {
    val dinners = Dinner.findAllDinners()
    Ok(views.html.Dinners.index(dinners))
  }

  def details(id: Long) = Action {
    val dinner = Dinner.findById(id)
    dinner match {
      case Some(_) => Ok(views.html.Dinners.details(dinner.get))
      case _ => NotFound
    }
  }
}
