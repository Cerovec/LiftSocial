package code.snippet

import net.liftweb._
import common.{Box,Full,Empty,Failure}
import scala.xml._
import util._
import Helpers._
import http._
import SHtml._
import mapper._
import js._
import JsCmds._
import JE._

import code.model
import code.comet.ActivityMaster

class PublishPost {
  def currUser = UserHelpers.getUser(UserHelpers.currUserId)

  def render = {
    def process(s: String) : JsCmd = {
      Thread.sleep(200);

      currUser match {
	case Full(u) => {
	  if (s != "") {
	    ActivityMaster.createPost(u, s)
	    
	  }
	}
	case _ => {
	  S.notice("Cannot post")
	}
      }
    }

    ".textbox" #> SHtml.onSubmit(s => {
      process(s) &
      SetValById("txtInput", "")
    })
  }
}
