package models

/**
 * @author maba, 2013-08-15
 */
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