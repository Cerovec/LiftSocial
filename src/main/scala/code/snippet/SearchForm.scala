package code.snippet

import net.liftweb._
import common.{Box,Full,Empty}
import scala.xml._
import util._
import Helpers._
import http._
import SHtml._
import js._
import JsCmds._
import JE._

import code.model._
import code.comet.SearchMaster

class SearchForm {
  def homepage = "http://localhost:8080"

  def render = {
    def process(s: String): JsCmd = {
      Thread.sleep(300)
      SearchMaster.makeSearch(s)
    }

    "name=query" #> SHtml.onSubmit(s => process(s))
  }
}
