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

import code.model._
import code.comet.ActivityMaster

class PublishBlogPost extends StatefulSnippet {
  def currUser = User.currentUser

  var subject = "";
  var text = "";
  var filebox: Box[FileParamHolder] = Empty
  var keywords = "";

  var max = 1; // 1MB

  // StatefulSnippet requires an explicit dispatch
  // to the method.
  def dispatch = {case "render" => render}

  def render = {
    def processKeywords(keywords: String): List[String] = {
      Nil
    }

    def process(): JsCmd = {
      currUser match {
	case Full(u) => {
	  if (subject == "") {
	    S.error("You must specify post subject")
	  } else if (text == "") {
	    S.error("You must specify post body")
	  } else if (filebox.isDefined && 
		     filebox.open_!.file.length > max*1024*1024) {
	    S.error("No files larger than " + max.toString + " MB")
	  } else {
	    Thread.sleep(300)

	    if (filebox.isDefined && 
		(filebox.open_!.file == null || filebox.open_!.file.length == 0)) {
	       // delete empty files
	       filebox = Empty
	    }
	    println("\n\n" + subject + "\n" + text + "\n" + keywords + "\n" + filebox.toString + "\n\n")

	    var keywordsList: List[String] = processKeywords(keywords)

	    ActivityMaster.createBlogPost(u, subject, text, filebox, keywordsList)
	    S.redirectTo("/");
	  }
	}
	case _ => {
	  S.notice("Cannot post")
	}
      }
    }

    "#subjectInput" #> SHtml.textarea(subject, subject = _) &
    "#blogInput" #> SHtml.textarea(text, text = _) &
    "type=file" #> SHtml.fileUpload(f => {filebox = Full(f)}) &
    "#keywordsInput" #> SHtml.textarea(keywords, keywords = _) &
    "type=submit" #> SHtml.onSubmitUnit(process)
  }
}
