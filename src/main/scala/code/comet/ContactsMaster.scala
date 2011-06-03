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

object ContactsMaster extends LiftActor with ListenerManager  {
  def createUpdate = ()

  override def lowPriority = {
    case p: ContactSig => updateListeners(p)
    case p: MoreSuggestionsSig => updateListeners(p)
    case p: ResetSuggestionsNumSig => updateListeners(p)
  }

  def addContact(user: User, contact: User) {
    if (user.addContact(contact) == 0) {
      S.notice("You have added " + user.firstName + " to your contacts");
    } else {
      S.notice("User is already on your contact's list");
    }

    updateListeners(new ContactSig())
  }

  def removeContact(user: User, contact: User) {
    user.removeContact(contact);
    S.notice("You have removed " + user.firstName + " from your contacts")
    
    updateListeners(new ContactSig())
  }

  def moreSuggestions() {
    updateListeners(new MoreSuggestionsSig())
  }

  def lessSuggestions() {
    updateListeners(new ResetSuggestionsNumSig())
  }
}

case class ContactSig()
case class MoreSuggestionsSig()
case class ResetSuggestionsNumSig()
