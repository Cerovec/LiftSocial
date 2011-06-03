package code.comet

import net.liftweb._
import http._
import util._
import Helpers._
import common.{Box, Full, Empty, Failure}
import mapper._
import js._
import JsCmds._
import JE._

import code.model._
import code.snippet._

class PokeViewer extends CometActor with CometListener {
  def registerWith = PokeMaster

  /**
   * Responds to messages
   */
  override def lowPriority = {
    case p : PokeSig => {
      if (User.currentUser == p.poke.toUser.obj ||
	  User.currentUser == p.poke.fromUser.obj) {
	println("\n\nObradjujem poke\n\n" + p.toString)
	println("\nA ja sam " + User.currentUser + "\n")
	println("\nA onaj drugi je " + p.poke.toUser.obj + "\n")
	val pokes = User.currentUser.get.getIncomingPokes()

	println("Imam " + pokes.length + " novih pokeova\n\n\n")
	reRender(true)
      }
    }
  }

  def renderPoke(poke : Poke) = {
    def processRemove(): JsCmd = {
      PokeMaster.deletePoke(poke)
      S.notice("You have removed the poke")
    }

    def processPokeBack(): JsCmd = {
      PokeMaster.pokeBack(poke)
    }

    ".pokelink [href]" #> 
      ("profile?id=" + (poke.fromUser.obj.map(_.id) openOr "")) &
    ".pokelink *" #> (poke.fromUser.obj.map(_.shortName) openOr "") &
    ".removebtn" #> SHtml.ajaxButton(
	    "Remove", () => {processRemove; }) &
    ".pbackbtn" #> SHtml.ajaxButton(
	    "Poke back", () => {processPokeBack; })
  }

  /**
   * Renders the poke viewer
   */
  def render = {
    val currUser = User.currentUser

    currUser match {
      case Full(cu) => {
	val pokes = cu.getIncomingPokes
	println("\n\n\nRendering " + pokes.length + " pokes\n\n\n")
	if (pokes.length > 0) {
	  ".pokeResult *" #> pokes.map(poke => renderPoke(poke))
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
