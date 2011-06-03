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

object PokeMaster extends LiftActor with ListenerManager  {
  def createUpdate = ()

  override def lowPriority = {
    case p : PokeSig => updateListeners(p)
  }

  def createPoke(from: User, to: User) = {
    val existingPokes: List[Poke] = 
      Poke.findAll(By(Poke.fromUser, from), By(Poke.toUser, to))
    
    if (existingPokes.length == 0) {
      var poke = Poke.create
      poke.fromUser(from)
      poke.toUser(to)
      poke.time(new Date())
      poke.save
      S.notice("You have successfully poked " + to.firstName)
      val pokes = to.getIncomingPokes()

      println("\n\n\nBitno: onaj drugi ima spremljenih " + pokes.length + 
	      " novih pokeova\n\n\n")

      updateListeners(new PokeSig(poke))
    } else
      S.notice("Your last poke is still pending")
  }

  def deletePoke(poke: Poke) {
    poke.delete_!
    S.notice("You have removed the poke")
    updateListeners(new PokeSig(poke))
  }

  def pokeBack(poke: Poke) {
    var us = (poke.fromUser.obj, poke.toUser.obj)
	
    us match {
      case (Full(a), Full(b)) => {
	poke.delete_!
	PokeMaster.createPoke(b, a)
      }
      case _ =>
	S.notice("The poke currently cannot be processed")
    }
  }
}

case class PokeSig(poke : Poke)
