package code.comet

import net.liftweb._
import http._
import SHtml._
import util._
import Helpers._
import common.{Box, Full, Empty, Failure}
import mapper._
import js._
import JsCmds._
import JE._
import scala.xml._

import code.model._
import code.snippet._

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar

abstract class ActivityViewer extends {

  val currentUser = User.currentUser

  val default = 10;
  val increase = 10;
  var query = default

  
  def renderRemoveBtn(post: Activity): CssBindFunc = {
    def remove(): JsCmd = {
      for (cu <- currentUser) {
	ActivityMaster.removeActivity(cu, post)
      }
      Noop
    }

    "type=submit" #> ajaxButton("Delete", () => {remove();})
  }

  def deleteRemoveBtn: CssBindFunc = {
    ".activityForm" #> ClearNodes
  }

  def fullRenderRemoveBtn(post: Activity): CssBindFunc = {
    if (currentUser == post.user.obj) {
      renderRemoveBtn(post)
    } else { 
      deleteRemoveBtn
    }
  }

  def renderActivityPic: CssBindFunc = {
    ".activityImg" #> ClearNodes
  }

  def deleteActivityPic: CssBindFunc = {
    ".activityImg" #> ClearNodes
  }

  def renderKeywords: CssBindFunc = {
    ".keywords *" #> ClearNodes
  }

  def deleteKeywords: CssBindFunc = {
    ".keywords" #> ClearNodes
  }

  def renderComments: CssBindFunc = {
    ".comments *" #> ClearNodes
  }

  def deleteComments: CssBindFunc = {
    ".comments" #> ClearNodes
  }
  
  def renderSubject(s: String): CssBindFunc = {
    ".subject *" #> makeRenderableSubject(s)
  }

  def deleteSubject: CssBindFunc = {
    ".subject" #> ClearNodes
  }

  // TODO: just too ugly, rewrite
  def renderTime(time: Date): CssBindFunc = {
    var cmp = new GregorianCalendar

    // seconds ago
    cmp.roll(Calendar.MINUTE, false)
    if (cmp.getTime before time) {
      ".time *" #> (((new Date().getTime - time.getTime())/1000).toInt.toString + 
	  " seconds ago")
    } else {
      //minutes ago
      cmp.roll(Calendar.MINUTE, true)
      cmp.roll(Calendar.HOUR, false)
      if (cmp.getTime before time) {
	".time *" #> (((new Date().getTime - time.getTime()) / 1000 / 60).
	       toInt.toString + " minutes ago")
      } else {
	// hours ago
	cmp.roll(Calendar.HOUR, true)
	cmp.set(Calendar.HOUR, 0);
	if (cmp.getTime before time) {
	  ".time *" #> (((new Date().getTime - time.getTime()) / 1000 / 60 / 24).
		      toInt.toString + " hours ago")
	} else {
	  if (today.getTime.getYear == time.getYear) {	
	    // usual
	    var df = new SimpleDateFormat("MMMM dd. HH:mm")
	    ".time *" #> df.format(time)
	  } else {
	     // usual
	    var df = new SimpleDateFormat("MMMM dd. yyyy. HH:mm")
	    ".time *" #> df.format(time)
	  }
	}
      }
    }
  }

  def makeRenderableText(text: String): NodeSeq = {
    val sp = text.split("\n")
    sp.map(a => <p>{a}</p>) toSeq
  }

  def makeRenderableSubject(text: String): NodeSeq = {
    {text split '\n' map { Text(_) ++ <br/> } reduceLeft (_ ++ _) }
  }

  def renderFile(activity: Activity) = {
    activity.file.obj match {
      case Full(file) => ".filelink [href]" #> RendererHelper.getFileUrl(file);
      case _ => ".uploadedFile" #> ClearNodes
    }
  }

  def renderPost(post: Activity) = {
    ".blogtext *" #> makeRenderableSubject(post.text.is) &
    deleteSubject &
    deleteKeywords &
    deleteActivityPic
  }

   def renderBlog(blog: Activity) = {
    ".blogtext *" #> makeRenderableText(blog.text.is) &
    renderSubject(blog.subject.is) &
    deleteKeywords &
    deleteActivityPic
  }

  def renderActivity(user: User, activity: Activity) = {
    var acType = activity.activityType.obj
    var acUser = activity.user.obj
    if (acUser.isEmpty ||
	acType.isEmpty) {
      ".activity" #> ClearNodes
    } else {
      ".name [href]" #> 
      ("profile?id=" + (acUser.map(_.id) openOr "")) &
      (".name *" #> (acUser.map(_.shortName) openOr "")) &
      (renderTime(activity.time.is)) &
      (fullRenderRemoveBtn(activity)) &
      renderComments &
      renderFile(activity) &
      ".prpicThumb [src]" #> 
	RendererHelper.getProfilePictureUrl(acUser.open_!) &
      (acType.map(_.typeString.is) match {
	case Full("post") => renderPost(activity)
	case Full("blog") => renderBlog(activity)
	case _ => "*" #> ClearNodes
      })
    }
  }
}

class HomeActivityViewer extends ActivityViewer 
    with CometActor with CometListener {
  
  def registerWith = ActivityMaster

  var appendActivity: Box[Activity] = Empty

  /**
   * Responds to messages
   */
  override def lowPriority = {
    case p: ActivitySig => {
      val userPost = p.activity.user.obj
      for {user <- currentUser
	   poster <- userPost} {
	     if (user == poster || user.isUsersContact(poster)) {
	       appendActivity = Full(p.activity)
	       reRender(true)
	       appendActivity = Empty
	     }
	   }
    }
    case p: MoreActivitiesSig => {
      for {cu <- currentUser
	   other <- p.user}
	if (cu == other) {
	  query += increase
	  reRender(true)
	}
      
    }
    case p: LessActivitiesSig => {
      for {cu <- currentUser
	   other <- p.user}
	if (cu == other) {
	  query = default
	  reRender(true)
	}
    }
    case r: RefreshHome => {
      for {cu <- currentUser}
	if (cu == r.user) {
	  reRender(true)
	}
    }
  }

  /**
   * Renders the activity list
   */
  def render = {
    currentUser match {
      case Full(u) => {
	if (appendActivity.isEmpty) {
	  ".activity *" #> {     
	    u.getObservableActivities(query).map(post => renderActivity(u, post))
	  }
	} else {
	  ".activity -*" #> {     
	    appendActivity.map(post => renderActivity(u, post))
	  }
	}
      }
      case _ => "*" #> ClearNodes
    }
  }
}
