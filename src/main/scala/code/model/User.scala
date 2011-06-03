package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.java.math.MathContext
import _root_.scala.xml.transform._
import _root_.net.liftweb.util.Helpers._ 
import java.text.DateFormat

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] with LongKeyedMetaMapper[User] {

  override def dbTableName = "Users" // define the DB table name

  override def screenWrap = Full(<lift:surround with="frame" at="content">
                                   <lift:bind/>
                                 </lift:surround>)

  override def signupFields = List(firstName, lastName, email, password);
  override def editFields = List(firstName, lastName, email, password, sex, currentCity, hometown, birthday, aboutMe);

  protected def profileFields = List(currentCity, hometown)

  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email, password);

  // comment this line out to require email validations
  override def skipEmailValidation = true;

  override lazy val testLogginIn = If(loggedIn_? _, () => RedirectResponse("login"));

  /** 
   * The LocParams for the menu item for login. 
   * Overwrite in order to add custom LocParams.
   * Attention: Not calling super will change the default behavior! 
   */
  override def loginMenuLocParams: List[LocParam[Unit]] =
    If(notLoggedIn_? _, S.??("already.logged.in")) ::
      Template(() => wrapIt(login)) ::
      Hidden ::
      LocGroup("account") ::
      Nil

  /** 
   * The LocParams for the menu item for logout. 
   * Overwrite in order to add custom LocParams.
   * Attention: Not calling super will change the default behavior! 
   */
  override def logoutMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(logout)) ::
      testLogginIn ::
      Hidden ::
      LocGroup("account") ::
      Nil

  /** 
   * The LocParams for the menu item for creating the user/sign up. 
   * Overwrite in order to add custom LocParams.
   * Attention: Not calling super will change the default behavior! 
   */
  override def createUserMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(signupFunc.map(_()) openOr signup)) ::
      If(notLoggedIn_? _, S.??("logout.first")) ::
      Hidden ::
      LocGroup("account") ::
      Nil

  /** 
   * The LocParams for the menu item for lost password. 
   * Overwrite in order to add custom LocParams.
   * Attention: Not calling super will change the default behavior! 
   */
  override def lostPasswordMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(lostPassword)) ::
      If(notLoggedIn_? _, S.??("logout.first")) ::
      Hidden ::
      Nil

  /** 
   * The LocParams for the menu item for resetting the password. 
   * Overwrite in order to add custom LocParams.
   * Attention: Not calling super will change the default behavior! 
   */
  override def resetPasswordMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(passwordReset(snarfLastItem))) ::
      If(notLoggedIn_? _, S.??("logout.first")) ::
      Hidden ::
      Nil

  /** 
   * The LocParams for the menu item for editing the user. 
   * Overwrite in order to add custom LocParams.
   * Attention: Not calling super will change the default behavior! 
   */
  override def editUserMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(editFunc.map(_()) openOr edit)) ::
      testLogginIn ::
      Hidden ::
      LocGroup("account") ::
      Nil

   /** 
   * The LocParams for the menu item for changing password. 
   * Overwrite in order to add custom LocParams.
   * Attention: Not calling super will change the default behavior! 
   */
  override def changePasswordMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(changePassword)) ::
      testLogginIn ::
      Hidden ::
      LocGroup("account") ::
      Nil

  /** 
   * The LocParams for the menu item for validating a user. 
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior! 
   */
  override def validateUserMenuLocParams: List[LocParam[Unit]] =
    Hidden ::
      Template(() => wrapIt(validateUser(snarfLastItem))) ::
      If(notLoggedIn_? _, S.??("logout.first")) ::
      Nil

  /** 
   * Override this method to do something else after the user signs up 
   */  
  override def actionsAfterSignup(theUser: TheUserType, func: () => Nothing): Nothing = {  
    theUser.setValidated(skipEmailValidation).resetUniqueId()
    theUser.save
    if (!skipEmailValidation) {  
      sendValidationEmail(theUser)  
      S.notice(S.??("sign.up.message"))  
      func()  
    } else {  
      logUserIn(theUser, () => {        
        S.notice(S.??("welcome"))  
        func()  
      })  
    }  
  }

  def editProfilePage = "editprofile"

  override def signup = {  
    val theUser: TheUserType = mutateUserOnSignup(createNewUserInstance())  
    val theName = signUpPath.mkString("")  
    
    def testSignup() {  
      validateSignup(theUser) match {  
        case Nil =>  
          actionsAfterSignup(theUser, () => S.redirectTo(homePage))  
  
        case xs => S.error(xs) ; signupFunc(Full(innerSignup _))  
      }  
    }  
    
    def innerSignup = bind("user",  
                           signupXhtml(theUser),  
                           "submit" -> SHtml.submit(S.??("sign.up"), testSignup _))  
    
    innerSignup  
  }

  object loginReferer extends SessionVar("/") 

  override def homePage = { 
    var ret = loginReferer.is 
    loginReferer.remove() 
    ret 
  } 
  
  override def login = { 
    for (r <- S.referer if loginReferer.is == "/") loginReferer.set(r) 
    super.login 
  } 

  override val basePath: List[String] = "account" :: Nil
  override def signUpSuffix = "signup"
  override def lostPasswordSuffix = "lostpassword"
  override def passwordResetSuffix = "resetpassword"
  override def changePasswordSuffix = "changepassword"
  override def validateUserSuffix = "validateuser"

  def findUsersLike(s: String): List[User] = {
    val foundFirstNames = User.findAll(
      Cmp(User.firstName, OprEnum.Like, 
	  Full(s.toLowerCase + "%"), None, Full("LOWER")))
    val foundLastNames = User.findAll(
        Cmp(User.lastName, OprEnum.Like, 
	  Full(s.toLowerCase + "%"), None, Full("LOWER")))
    val foundAboutMes = User.findAll(
       Cmp(User.aboutMe, OprEnum.Like, 
	  Full(s.toLowerCase + "%"), None, Full("LOWER")))

    foundFirstNames union foundLastNames union foundAboutMes distinct
  }
}

/**
 * An O-R mapped "User" class that includes first name, last name,
 * password and we add a "Personal Essay" to it
 */
class User extends MegaProtoUser[User] with LongKeyedMapper[User] with OneToMany[Long, User] with ManyToMany {
  def getSingleton = User // what's the "meta" server

  object currentCity extends MappedString(this, 160) {
    override def displayName = "Current City";
  }

  object hometown extends MappedString(this, 160) {
    override def displayName = "Hometown";
  }

  object sex extends MappedGender(this) {
    override def displayName = "I am";
  }
  
  object birthday extends MappedDate(this) {
    final val dateFormat = 
      DateFormat.getDateInstance(DateFormat.SHORT)
    override def displayName = "Birthday";
  }

  // define an additional field for a personal essay
  object aboutMe extends MappedTextarea(this, 1024) {
    override def textareaRows = 8
    override def textareaCols = 70
    override def displayName = "Summary"
  }

  object profilePicture extends LongMappedMapper(this, File) {
    override def defaultValue = -1
  }

  /**
   * Function adds a contact to a user.
   * Returns 0 if contact created od 1 if contact already existed
   */
  def addContact(contact: User): Int = {
    val existing = Contact.findAll(By(Contact.user, this), By(Contact.contact, contact))
    if (existing.length == 0) {
      Contact.join(this, contact)
      0
    } else {
      1
    }
  }


  /**
   * Removes a contact of a user.
   * Returns 0 if successfull, else 1
   */
  def removeContact(contact: User) : Int = {
    var found = Contact.findAll(By(Contact.user, this), 
				By(Contact.contact, contact))
    found.map(_.delete_!)
    if (found.length > 0) 0 else 1
  }

  // gets the list of all contacts
  def getContacts(): List[User] = {
    var found: List[Contact] = Contact.findAll(By(Contact.user, this))
    for {contact <- found
	 user <- contact.contact.obj}
      yield user
  }

  def getRecommendedContacts(n: Int): List[User] = {
    var contacts = User.findAll()
    contacts = contacts.filterNot(_ == this)
    contacts = contacts.filterNot(this.getContacts().contains(_))
    contacts = contacts.sortWith(this.getMutualContacts(_).length > 
				 this.getMutualContacts(_).length)
    contacts.take(n)
  }

  def getMutualContacts(user: User): List[User] = {
    val firstContacts: List[User] = this.getContacts()
    val secContacts:List[User] = user.getContacts()
    
    firstContacts.intersect(secContacts)
  }

  def isContactToUser(user: User) : Boolean = {
    Contact.findAll(By(Contact.user, user), 
		    By(Contact.contact, this)).length > 0
  }

  def isUsersContact(user: User) : Boolean = {
    Contact.findAll(By(Contact.user, this), 
		    By(Contact.contact, user)).length > 0
  }

  def getActivities() = {
    Activity.findAll(By(Activity.user, this), OrderBy(Activity.time, Descending))
  }

  def getActivities(n: Int) = {
    Activity.findAll(By(Activity.user, this), 
		     OrderBy(Activity.time, Descending)) take n
  }

  def addActivity(a: Activity) = {
    a.user(this)
    a.save
  }

  def removeActivity(a: Activity) = {
    a.delete_!
  }

  def getObservableActivities(n: Int): List[Activity] = {
    val contacts = getContacts
    val ret = contacts flatMap(_.getActivities) union getActivities 
    ret sortWith ((a, b) => a.time.is.compareTo(b.time.is) > 0) take n
  }

  def getIncomingPokes() = {
    Poke.findAll(By(Poke.toUser, this))
  }

  def isPokedBy(user: User) = {
    Poke.findAll(By(Poke.toUser, this), By(Poke.fromUser, user)).length > 0
  }
}

