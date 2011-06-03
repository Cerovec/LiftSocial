package code.snippet

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

import code.comet.ActivityViewer
import code.model._
import code.snippet._

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar

class ProfileActivityViewer extends ActivityViewer {
  /**
   * Renders the activity list
   */
  def render = {
    val us = UserHelpers.getUser(UserHelpers.userId)

    us match {
      case Full(u) => {
	val activities = u.getActivities()
	if (activities.length > 0) {
	  ".activity *" #> activities.map(post => renderActivity(u, post))
	} else {
	  "*" #> ClearNodes
	}
      }
      case _ => {
	"*" #> ClearNodes
      }
    }
  }
}
