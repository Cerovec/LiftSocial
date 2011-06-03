package code.model

import _root_.org.specs._
import _root_.org.specs.runner.JUnit4
import _root_.org.specs.runner.ConsoleRunner
import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._

import Helpers._

class UserSpecsAsTest extends JUnit4(UserSpecs)
//object UserSpecsRunner extends ConsoleRunner(UserSpecs)

object UserSpecs extends Specification {
  val provider = DBProviders.PostgreSqlProvider
  
  private def ignoreLogger(f: => AnyRef): Unit = ()

  def setupDB {
    MapperRules.createForeignKeys_? = c => false
    provider.setupDB
    Schemifier.destroyTables_!!(ignoreLogger _,  
	User, Contact, Activity, ActivityType, File, Poke)
    Schemifier.schemify(true, ignoreLogger _, 
	User, Contact, Activity, ActivityType, File, Poke)
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

  "User" should {
    "save itself in the database" in {
      setupDB
      var user = createUser("Ivan", "Dokovic");
      user.save

      val found = User.find(By(User.firstName, "Ivan"), 
			    By(User.lastName, "Dokovic"))
      found must beLike {case Full(u) => u.email == "Ivan.Dokovic@gmail.com" }
      
      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "know how to add contacts" in {
      setupDB
      val userNovak = createUser("Novak", "Dokovic")
      userNovak.save

      val userRoger = createUser("Roger", "Federer")
      userRoger.save

      // novak successfully adds roger
      var stat = userNovak.addContact(userRoger)
      stat must beEqualTo(0)

      var found = Contact.findAll(By(Contact.user, userNovak),
				  By(Contact.contact, userRoger))
      found.length must beEqualTo(1)

      // roger successfully adds novak
      stat = userRoger.addContact(userNovak)
      stat must beEqualTo(0)

      found = Contact.findAll(By(Contact.user, userRoger),
			      By(Contact.contact, userNovak))
      found.length must beEqualTo(1)

      // roger unsuccessfully tries to add novak again
      stat = userRoger.addContact(userNovak)
      stat must beEqualTo(1)

      found = Contact.findAll(By(Contact.user, userRoger),
			      By(Contact.contact, userNovak))
      found.length must beEqualTo(1)
      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "know how to remove contacts" in {
      setupDB
      val userNovak = createUser("Novak", "Dokovic")
      userNovak.save

      val userRoger = createUser("Roger", "Federer")
      userRoger.save

      // novak successfully adds roger
      var stat = userNovak.addContact(userRoger)
      stat must beEqualTo(0)

      var found = Contact.findAll(By(Contact.user, userNovak),
				  By(Contact.contact, userRoger))
      found.length must beEqualTo(1)

      var contacts = userNovak.getContacts()
      contacts.length must be(1)

      // novak successfully removes roger
      stat = userNovak.removeContact(userRoger)
      stat must beEqualTo(0)

      contacts = userNovak.getContacts()
      contacts.length must be(0)

      // novak unsuccessfully tries to remove roger
      stat = userNovak.removeContact(userRoger)
      stat must beEqualTo(1)

      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "retrieve it's contacts" in {
      setupDB
      val userNovak = createUser("Novak", "Dokovic")
      userNovak.save

      val userRoger = createUser("Roger", "Federer")
      userRoger.save

      val userAndy = createUser("Andy", "Murray")
      userAndy.save

        // novak successfully adds roger
      var stat = userNovak.addContact(userRoger)
      userNovak.addContact(userAndy)

      var contacts: List[User] = userNovak.getContacts()
      contacts.length must beEqualTo(2)

      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "know if it's contact to a certain user" in {
      setupDB
      val userNovak = createUser("Novak", "Dokovic")
      userNovak.save

      val userRoger = createUser("Roger", "Federer")
      userRoger.save

      val userAndy = createUser("Andy", "Murray")
      userAndy.save

      // novak adds roger
      var stat = userNovak.addContact(userRoger)
      userNovak.isUsersContact(userRoger) must be(true)
      userNovak.isUsersContact(userAndy) must be(false)
      userRoger.isContactToUser(userNovak) must be(true)
      userRoger.isUsersContact(userNovak) must be(false)

      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "find mutual contacts" in {
      setupDB
      val userNovak = createUser("Novak", "Dokovic")
      userNovak.save

      val userRoger = createUser("Roger", "Federer")
      userRoger.save

      val userAndy = createUser("Andy", "Murray")
      userAndy.save

      val userGael = createUser("Gael", "Monfils")
      userGael.save

         // novak successfully adds roger
      var stat = userNovak.addContact(userRoger)
      stat = userNovak.addContact(userAndy)
      stat = userGael.addContact(userAndy)

      var contacts: List[User] = userNovak.getMutualContacts(userGael)
      contacts.length must be(1)

      stat = userGael.addContact(userNovak)
      contacts = userNovak.getMutualContacts(userGael)
      contacts.length must be(1)

      userGael.addContact(userRoger)
      contacts = userNovak.getMutualContacts(userGael)
      contacts.length must be(2)

      userNovak.removeContact(userAndy)
      contacts = userNovak.getMutualContacts(userGael)
      contacts.length must be(1)

      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "know it's recommended contacts" in {
      setupDB

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

      userNovak.addContact(userRoger)
      userNovak.addContact(userAndy)
      
      userGael.addContact(userRoger)
      userGael.addContact(userAndy)

      userDavid.addContact(userTsonga)

      userTsonga.addContact(userAndy)

      var contacts = userNovak.getRecommendedContacts(10)

      contacts.length must be(3)
      contacts(0).email.is must beMatching("gael.monfils@gmail.com")
      contacts(1).email.is must beMatching("wilfried.tsonga@gmail.com")
      contacts(2).email.is must beMatching("david.nalbandian@gmail.com")

      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "knows how to search for users" in {
      setupDB

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

      var found = User.findUsersLike("Gael")
      found.length must be(1)
      found(0).email.is must beMatching("gael.monfils@gmail.com")

      found = User.findUsersLike("Gae")
      found.length must be(1)
      found(0).email.is must beMatching("gael.monfils@gmail.com")
      
      found = User.findUsersLike("tsong")
      found.length must be(1)
      found(0).email.is must beMatching("wilfried.tsonga@gmail.com")
      
      found = User.findUsersLike("Spasoje")
      found.length must be(0)

      found = User.findUsersLike("L")
      found.length must be(2)

      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "know how to add, remove and find his/her activities" in {
      setupDB

      val userNovak = createUser("Novak", "Dokovic")
      userNovak.save

      val userRoger = createUser("Roger", "Federer")
      userRoger.save

      val userAndy = createUser("Andy", "Murray")
      userAndy.save

      val userGael = createUser("Gael", "Monfils")
      userGael.save

      // activity not saved because no activitytype "post" doesnt exist
      var activity = Activity.createPost("Ovo je moj prvi blog post")

      for {a <- activity} {
	userNovak.addActivity(a)
      }

      var found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(0)
      found = userNovak.getActivities()
      found.length must be(0)
      
      // successful creation of post
      var t = createType("post")
      t.save

      activity = Activity.createPost("Ovo je moj prvi blog post")

      for {a <- activity} {
	userNovak.addActivity(a)
      }

      found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(1)
      found = userNovak.getActivities()
      found.length must be(1)

      // removing post
      // removing activity
      for {a <- activity} {
	userNovak.removeActivity(a)
      }
      
      found = userNovak.getActivities()
      found.length must be(0)

      found = Activity.findAll(By(Activity.user, userNovak))
      found.length must be(0)

      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }

    "find observable activities" in {
      setupDB

      val userNovak = createUser("Novak", "Dokovic")
      userNovak.save

      val userRoger = createUser("Roger", "Federer")
      userRoger.save

      val userAndy = createUser("Andy", "Murray")
      userAndy.save

      val userGael = createUser("Gael", "Monfils")
      userGael.save

      // create post type
      var t = createType("post")
      t.save

      val activity1 = Activity.createPost("Ovo je Novakov prvi post")
      for {a <- activity1} {
	userNovak.addActivity(a)
      }

      val activity2 = Activity.createPost("Ovo je Novakov drugi post")
      for {a <- activity2} {
	userNovak.addActivity(a)
      }

      val activity3 = Activity.createPost("Ovo je Rogerov prvi post")
      for {a <- activity3} {
	userRoger.addActivity(a)
      }

      val activity4 = Activity.createPost("Ovo je Andyjev prvi post")
      for {a <- activity4} {
	userAndy.addActivity(a)
      }

      val activity5 = Activity.createPost("Ovo je Rogerov drugi post")
      for {a <- activity5} {
	userRoger.addActivity(a)
      }

      userNovak.addContact(userRoger)
      userNovak.addContact(userAndy)
      userRoger.addContact(userNovak)
      userRoger.addContact(userAndy)
      

      var found = userNovak.getObservableActivities(10)
      
      found.length must be(5)
      found(0) must beEqualTo(activity5.open_!)
      found(1) must beEqualTo(activity4.open_!)
      found(2) must beEqualTo(activity3.open_!)
      found(3) must beEqualTo(activity2.open_!)
      found(4) must beEqualTo(activity1.open_!)

      found = userRoger.getObservableActivities(10)
      
      found.length must be(5)
      found(0) must beEqualTo(activity5.open_!)
      found(1) must beEqualTo(activity4.open_!)
      found(2) must beEqualTo(activity3.open_!)
      found(3) must beEqualTo(activity2.open_!)
      found(4) must beEqualTo(activity1.open_!)

      val activity6 = Activity.createPost("Ovo je Novakov treci post")
      for {a <- activity6} {
	userNovak.addActivity(a)
      }

      found = userNovak.getObservableActivities(10)
      
      found.length must be(6)
      found(0) must beEqualTo(activity6.open_!)

      found = userRoger.getObservableActivities(10)
      
      found.length must be(6)
      found(0) must beEqualTo(activity6.open_!)


      provider.vendor.releaseConnection(provider.vendor.mkConn)
    }
  }
}
