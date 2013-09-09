package models

import anorm.Pk

/**
 * @author maba, 2013-08-15
 */
case class RSVP(
                 id: Pk[Long],
                 dinnerId: Long,
                 attendeeName: String
                 )
