package models

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.cache.Cache
import play.api.mvc.Security

/**
 * @author mats, 2013-02-05
 */
case class User(username: String, email: String, password: String, id: Pk[Long] = NotAssigned)

object User {

  val userCacheKey = Security.username + "."
  val userCacheExpiration = 60 * 60

  val parser = {
    get[Pk[Long]]("users.UserId") ~
    get[String]("users.Username") ~
    get[String]("users.Email") ~
    get[String]("users.Password") map {
      case userId~username~email~password => User(username, email, password, userId)
    }
  }

  /**
   * Authenticate a User.
   */
  def authenticate(username: String, password: String): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from Users users
          where users.Username = {username}
          and users.Password = {password}
        """
      ).on(
        'username -> username,
        'password -> password
      ).as(parser.singleOpt)
    } match {
      case Some(_) => true
      case _ => false
    }
  }

  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      val userId: Option[Long] = SQL(
        """
          insert into Users(Username, Email, Password) values(
            {username}, {email}, {password}
          )
        """
      ).on(
        'username -> user.username,
        'email -> user.email,
        'password -> user.password
      ).executeInsert()
      user.copy(id = Id(userId.get))
    }
  }

  def findByName(username: String): Option[User] = {
    Cache.getOrElse[Option[User]](userCacheKey + username, userCacheExpiration) {
      DB.withConnection { implicit connection =>
        SQL(
          """
            select * from Users users
            where users.Username = {username}
          """
        ).on(
          'username -> username
        ).as(
          User.parser.singleOpt
        )
      }
    }
  }

}

