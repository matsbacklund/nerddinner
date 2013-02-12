package controllers

import play.api.mvc._
import models.Dinner
import play.api.libs.json.Json

/**
 * @author backlmat, 2013-02-12
 */
object Rsvps extends Controller with Secured {

  def register(dinnerId: Long) = IsAuthenticated { username => implicit request =>
    Dinner.isUserRegistered(dinnerId, username) match {
      case false => {
        Dinner.addUser(dinnerId, username)
      }
    }
    Ok("Thanks - we'll see you there!")
  }
}
