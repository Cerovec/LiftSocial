package code.comet

import net.liftweb._
import http._
import util._
import Helpers._
import actor._
import common.{Box, Full, Empty, Failure}
import java.util.Date
import mapper._

import code.model._

object ActivityMaster extends LiftActor with ListenerManager  {
  def createUpdate = ()

  override def lowPriority = {
    case p : ActivitySig => updateListeners(p)
    case r : RefreshHome => updateListeners(r)
  }

  def createPost(from: User, message: String) = {
    val post = Activity.createPost(message)
    post match {
      case Full(p) => {
	from.addActivity(p)
	updateListeners(new ActivitySig(p))
      }
      case _ => {
	S.notice("Cannot write comment");
      }
    }
  }

  def createBlogPost(from: User, subject: String, text: String, 
		     filebox: Box[FileParamHolder], keywordsList:List[String]) = {
    val blog = Activity.createBlogPost(subject, text, filebox, keywordsList)
    blog match {
      case Full(p) => {
	from.addActivity(p)
	updateListeners(new ActivitySig(p))
      }
      case _ => {
	S.notice("Cannot write blog post");
      }
    }
  }

  def removeActivity(user: User, activity: Activity) = {
    user.removeActivity(activity)
    updateListeners(new ActivitySig(activity))
  }

  def moreActivities(user: Box[User]) {
    updateListeners(new MoreActivitiesSig(user))
  }

  def lessActivities(user: Box[User]) {
    updateListeners(new LessActivitiesSig(user))
  }
}

case class ActivitySig(activity : Activity)
case class MoreActivitiesSig(user: Box[User])
case class LessActivitiesSig(user: Box[User])
case class RefreshHome(user: User)
