package code.snippet

import net.liftweb._
import util.Helpers._
import code.model.User
import _root_.net.liftweb.common._ 
import scala.xml.NodeSeq

/**
 * A snippet for choosing the homepage
 */
class MenuChooser {
  def render = {
    if (User.loggedIn_?)
      <span class="lift:Menu.builder"></span> ::
      <hr class="space" /> ::
      Nil
    else
      NodeSeq.Empty
  } 
}
