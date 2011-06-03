package code.snippet

import net.liftweb._
import util.Helpers._
import code.model.User

/**
 * A snippet for choosing the homepage
 */
class HomepageChooser {
  def render = {
    if (User.loggedIn_?)
      <span class="lift:embed?what=home" />
    else
      <span class="lift:embed?what=welcome" />
  } 
}
