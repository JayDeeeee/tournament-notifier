package de.janik.dotzel.tournament_notifier

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import sttp.client._

// TODO Für jeden Monat muss ein Scraper laufen

object Main extends App {

  def scrapeByUrl(url: String): Unit = {
    // Scrape
    val scraper = new Scraper(url)
    val events = scraper.getCurrentEvents()
    val ufrEvents = scraper.filterEvents(events, "Unterfranken", 4)

    // Check for changes
    val monitor = new Monitor(url)
    monitor.updateEvents(ufrEvents)
    if (monitor.hasChanged) monitor.notifyTelegram("1086614510:AAGNMEE9gRnHACDDC7L35CnY3E2O700Ao0w", url)
  }

  def getUrlList(): List[String] = {
    val urlwithoutDate = "https://bttv.click-tt.de/cgi-bin/WebObjects/nuLigaTTDE.woa/wa/tournamentCalendar?circuit=2020_BTTR&federation=ByTTV&date="
    val urlJanuary = urlwithoutDate + "2020-01-01"
    val urlFebruary = urlwithoutDate + "2020-02-01"
    val urlMarch = urlwithoutDate + "2020-03-01"
    val urlApril = urlwithoutDate + "2020-04-01"
    val urlMay = urlwithoutDate + "2020-05-01"
    val urlJune = urlwithoutDate + "2020-06-01"
    val urlJuli = urlwithoutDate + "2020-07-01"
    val urlAugust = urlwithoutDate + "2020-08-01"
    val urlSeptember = urlwithoutDate + "2020-09-01"
    val urlOctober = urlwithoutDate + "2020-10-01"
    val urlNovember = urlwithoutDate + "2020-11-01"
    val urlDezember = urlwithoutDate + "2020-12-01"
    List(urlJanuary, urlFebruary, urlMarch, urlApril, urlMay, urlJune, urlJuli, urlAugust, urlSeptember, urlOctober, urlNovember, urlDezember)
  }

  while(true) {
    for (url <- getUrlList ) yield scrapeByUrl(url)
    Thread.sleep(60000)
  }
}

class Scraper(url: String) {
  type WebElements = Vector[Vector[net.ruippeixotog.scalascraper.model.Element]]
  private final val browser = JsoupBrowser()
  private final val doc = browser.get(url)

  def getCurrentEvents(): WebElements = {
    doc >> extractor("#content-row2 > table.result-set", table)
  }

  def filterEvents(events: WebElements, criteria: String, column: Int): WebElements = {
    events.tail // The first entry is the table header, therefore it will be ignored
      .filter(_(column).toString.contains(criteria))
  }
}

class Monitor(url: String) {
  type WebElements = Vector[Vector[net.ruippeixotog.scalascraper.model.Element]]
  private var eventsOld: WebElements = Vector()
  private var eventsNew: WebElements = Vector()

  def updateEvents(events: WebElements): Unit = {
    eventsOld = eventsNew
    eventsNew = new Scraper(this.url).getCurrentEvents()
  }

  def hasChanged: Boolean = eventsOld == eventsNew

  def notifyTelegram(accessToken: String, url: String): Unit = {
    // Create Request
    val method = "sendMessage"
    val chat_id = "-426393772"
    val text = s"A new BTTR-Race has been published: $url"
    val request = basicRequest.get(uri"https://api.telegram.org/bot${accessToken}/${method}?chat_id=${chat_id}&text=${text}&disable_notification=false")

    // Sent request
    implicit val backend = HttpURLConnectionBackend()
    val response = request.send()

    println(response.body)
  }

  /*
  This method is not ready for production.
   */
  def getChatId(): Unit = {
    val request = basicRequest.get(uri"https://api.telegram.org/bot1086614510:AAGNMEE9gRnHACDDC7L35CnY3E2O700Ao0w/getUpdates")
    implicit val backend = HttpURLConnectionBackend()
    val response = request.send()

    println(response.body)
  }

}