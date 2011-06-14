package code.model

import _root_.org.specs._
import _root_.org.specs.runner.JUnit4
import _root_.org.specs.runner.ConsoleRunner
import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._

import Helpers._

class ActivitySpecsAsTest extends JUnit4(UserSpecs)
//object UserSpecsRunner extends ConsoleRunner(UserSpecs)

object ActivitySpecs extends Specification {
  val provider = DBProviders.PostgreSqlProvider
  
  private def ignoreLogger(f: => AnyRef): Unit = ()

  def setupDB {
    MapperRules.createForeignKeys_? = c => false
    provider.setupDB
    Schemifier.destroyTables_!!(ignoreLogger _,  User, Activity, ActivityType)
    Schemifier.schemify(true, ignoreLogger _, User, Activity, ActivityType)

    val userNovak = createUser("Novak", "Dokovic")
    userNovak.save

    val userRoger = createUser("Roger", "Federer")
    userRoger.save
    
    val userAndy = createUser("Andy", "Murray")
    userAndy.save
    
    val userGael = createUser("Gael", "Monfils")
    userGael.save
    
    val userDavid = createUser("David", "Nalbandian")
    userDavid.save
    
    val userTsonga = createUser("Wilfried", "Tsonga")
    userTsonga.save
    
    val userLukas = createUser("Lukas", "Lacko")
    userLukas.save
    
    val userLlodra = createUser("Michael", "Llodra")
    userLlodra.save
  }

  def createUser(name: String, surname: String) : User = {
    var user = User.create
    user.firstName(name)
    user.lastName(surname)
    user.email(name + '.' + surname + "@gmail.com")
    user.password("password")
    user.aboutMe("tenisac")
    user.validated(true)
  }

  def createType(s: String): ActivityType = {
    var activityType = ActivityType.create
    activityType.typeString(s)
  }

  "Activity" should {
    "create posts" in {
      setupDB

      // unsuccessful because ActicityType doesn't exist
      var userNovak = User.find(By(User.firstName, "Novak"))
      var activity = Activity.createPost("Ovo je moj prvi blog post")

      for {u <- userNovak
	   a <- activity} {
	u.addActivity(a)
      }

      var found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(0)

      for {u <- userNovak} {
	found = u.getActivities()
	found.length must be(0)
      }

      // successful creation of post
      var t = createType("post")
      t.save

      activity = Activity.createPost("Ovo je moj prvi blog post")
      for {u <- userNovak
	   a <- activity} {
	u.addActivity(a)
      }

      for {u <- userNovak} {
	found = u.getActivities()
	found.length must be(1)
      }

      found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(1)

      found = Activity.findAll(By(Activity.text, "Ovo je moj prvi blog post"))
      found.length must be(1)

      found = Activity.findAll(By(Activity.activityType, t))
      found.length must be(1)

      // removing activity
      for {u <- userNovak
	   a <- activity} {
	u.removeActivity(a)
      }
      
      for {u <- userNovak} {
	found = u.getActivities()
	found.length must be(0)
      }

      found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(0)

      
      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "create blog posts" in {
      setupDB

      // unsuccessful because ActicityType doesn't exist
      var userNovak = User.find(By(User.firstName, "Novak"))
      var activity = Activity.createBlogPost("Prva tema", 
					     "Ovo je moj prvi blog post", Empty, Nil)

      for {u <- userNovak
	   a <- activity} {
	u.addActivity(a)
      }

      var found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(0)

      for {u <- userNovak} {
	found = u.getActivities()
	found.length must be(0)
      }

      // successful creation of post
      var t = createType("blog")
      t.save

      activity = Activity.createBlogPost("Prva tema", "Ovo je moj prvi blog post", Empty, Nil)
      for {u <- userNovak
	   a <- activity} {
	u.addActivity(a)
      }

      for {u <- userNovak} {
	found = u.getActivities()
	found.length must be(1)
      }

      found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(1)

      found = Activity.findAll(By(Activity.text, "Ovo je moj prvi blog post"))
      found.length must be(1)

      found = Activity.findAll(By(Activity.activityType, t))
      found.length must be(1)

      // removing activity
      for {u <- userNovak
	   a <- activity} {
	u.removeActivity(a)
      }
      
      for {u <- userNovak} {
	found = u.getActivities()
	found.length must be(0)
      }

      found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(0)

      
      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }
  }
}
