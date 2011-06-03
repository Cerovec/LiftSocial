package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._

import code.snippet._
import code.model._
import net.liftweb.widgets.autocomplete.AutoComplete

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = new StandardDBVendor(Props.get("db.driver") 
					openOr "org.postgresql.Driver",
        Props.get("db.url") openOr "jdbc:postgresql:database",
        Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User, Poke, Contact, Activity,
		      ActivityType, Comment, File, FileType, ActivityLike, 
			ActivityKeyword, Keyword)

    LiftRules.dispatch.append(FileServicer.matcher) 

    // where to search snippet
    LiftRules.addToPackages("code")

    val loggedIn = If(() => User.loggedIn_?, () => RedirectResponse("account/login"))

    // Build SiteMap
    def sitemap(): SiteMap = SiteMap(
       // the simple way to declare a menu
      Menu(S ? "Home") / "index" >> User.AddUserMenusAfter,
      Menu(S ? "Profile") / "profile",
      Menu(S ? "Inbox") / "messages" >> loggedIn,
      Menu(S ? "Contacts") / "connections" >> loggedIn,
      Menu(S ? "Search") / "search",
      Menu(S ? "Change profile picture") / "changeprofilepic" >> Hidden >> loggedIn >> LocGroup("account"),
    
      // Bottom menu
      Menu(S ? "About") / "about" >> Hidden >> LocGroup("bottom"),
      Menu(S ? "Contact") / "contact" >> Hidden >> LocGroup("bottom"),
      Menu(S ? "Feedback") / "feedback" >> Hidden >> LocGroup("bottom"),
      Menu(S ? "Terms") / "terms" >> Hidden >> LocGroup("bottom"),
      Menu(S ? "Help") / "help" >> Hidden >> LocGroup("bottom"),
      Menu(S ? "Sitemap") / "sitemap" >> Hidden >> LocGroup("bottom"),

      //Menu(S ? "Edit Profile") / "editprofile" >> Hidden >> loggedIn >> LocGroup("account"),
         
      // allows anything in the static path to be visible
      Menu("Static") / "static" / ** >> Hidden) 

    def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap()))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
  }
}
