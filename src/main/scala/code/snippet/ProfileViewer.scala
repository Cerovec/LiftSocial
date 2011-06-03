package code.snippet

import net.liftweb._
import net.liftweb.common.{Box,Full,Empty,Failure}
import code.model.User
import scala.xml._
import net.liftweb.util._
import Helpers._
import _root_.net.liftweb.http._
import _root_.net.liftweb.mapper._

class ProfileViewer {
  def makeHeading = {
    var us = UserHelpers.getUser(UserHelpers.userId)
    us match {
      case Full(p) => ".heading *" #> p.shortName.concat("'s Profile")
      case _ => ".heading *" #> "The profile"
    }    
  }

  def renderError = {
    "* *" #> "Profile currently unavailable"
  }

  def getRenderableText(text: String): NodeSeq = {
    <p> {text split '\n' map { Text(_) ++ <br/> } reduceLeft (_ ++ _) } </p>
  }
  
  def render = {
    def renderProfile(prof: Box[User]) = {
      prof match {
	case Full(p) => {
	  val username = p.shortName
	  val email = p.email
	  ".heading *" #> p.shortName.concat("'s Profile") &
	  "#name *" #> p.shortName &
	  "#email *" #> p.email &
	  "#currCity *" #> p.currentCity.is &
	  "#hometown *" #> p.hometown.is &
	  "#sex *" #> p.sex &
	  ".prpicThumb [src]" #> RendererHelper.getProfilePictureUrl(p) &
	  (if (p.birthday == p.birthday.defaultValue) 
	    "#birthrow" #> ClearNodes 
	   else 
	     "#birthday *" #> p.birthday) &
	  "#aboutMe *" #> getRenderableText(p.aboutMe.is)
	}
	case _ => println("smece"); renderError
      }
    }

    renderProfile(UserHelpers.getUser(UserHelpers.userId))
  }
    
  def editProfile = {
    def edit(prof: Box[User]) = {
      prof match {
	case Full(p) => <table>{p.toForm(Full("Save"),"/profile")}</table>
	case _ => renderError
      }
    }

    edit(UserHelpers.getUser(UserHelpers.userId))
  }
}
