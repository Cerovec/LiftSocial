package code.snippet

import net.liftweb._
import util.Helpers._
import code.model.User

/**
 * A snippet for choosing the homepage
 */
class PokeListChooser {
  def render = {
    if (UserHelpers.userId == UserHelpers.currUserId)
      <span class="lift:embed?what=pokelist"></span>
    else
      Nil
  } 
}
