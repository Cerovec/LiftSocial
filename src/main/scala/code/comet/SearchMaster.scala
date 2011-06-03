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

object SearchMaster extends LiftActor with ListenerManager  {
  def createUpdate = ()

  override def lowPriority = {
    case p : SearchSig => updateListeners(p)
  }

  def makeSearch(s: String) = {
    updateListeners(new SearchSig(s))
  }
}

case class SearchSig(s: String)
