package code.snippet

import net.liftweb._
import net.liftweb.common.{Box,Full,Empty,Failure}
import code.model.{User, Contact}
import scala.xml._
import net.liftweb.util._
import Helpers._
import _root_.net.liftweb.http._
import _root_.net.liftweb.mapper._

object UserHelpers {
  /**
   * Method gets the user id from the url
   * */
  def userId : Box[Long] = S.param("id") match {
    case Full(ids) => Full(ids.toLong)
    case _ => currUserId
  }

  /**
   * Method gets the user id of a logged in user
   * */
  def currUserId : Box[Long] = User.currentUserId match {
    case Full(ids) => Full(ids.toLong)
    case _ => Empty
  }

  /**
   * Function gets the User object for a user with id id
   * */
  def getUser(id: Box[Long]): Box[User] = {
    id match {
      case Full(i) => User.find(By(User.id, i))
      case _ => Empty
    }
  }

 
  def contactOfAUser_?(userId: Long, contactId: Long): Boolean = 
    Contact.findAll(By(Contact.user, userId), By(Contact.contact, contactId)).length > 0
}
