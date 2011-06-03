package code.snippet

import net.liftweb._
import net.liftweb.common.{Box,Full,Empty,Failure}
import code.model._
import code.comet._
import scala.xml._
import net.liftweb.util._
import Helpers._
import _root_.net.liftweb.http._
import _root_.net.liftweb.mapper._
import js._
import JsCmds._
import JE._
import _root_.net.liftweb.http.SHtml._
import java.util.Date

class MoreSuggestionsButton {
  def render = {
    def processMore(): JsCmd = {
      ContactsMaster.moreSuggestions()
      Noop
    }
    
    "id=moreSuggBtn" #> 
      SHtml.ajaxButton("Show more suggestions", () => {processMore(); } )
  }
}
