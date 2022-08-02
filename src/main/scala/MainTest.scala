import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import sttp.client._

object MainTest extends App {

  def scrapeByUrl(url: String): Unit = {
    // Scrape
    val scraper = new Scraper2(url)
    val events = scraper.getCurrentEvents()
    val ufrEvents = scraper.filterEvents(events, "Unterfranken", 4)

    // Check for changes
    val monitor = new Monitor2(url)
    monitor.updateEvents(ufrEvents)
    if (monitor.hasChanged) monitor.notifyTelegram(${accessToken}, url)
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

  while (true) {
    scrapeByUrl("testPage.html")
    Thread.sleep(60000)
  }
}

class Scraper2(url: String) {
  type WebElements = Vector[Vector[net.ruippeixotog.scalascraper.model.Element]]
  private final val browser = JsoupBrowser()
  private final val doc = browser.parseFile(url)

  def getCurrentEvents(): WebElements = {
    doc >> extractor("#content-row2 > table.result-set", table)
  }

  def filterEvents(events: WebElements, criteria: String, column: Int): WebElements = {
    events.tail // The first entry is the table header, therefore it will be ignored
      .filter(_(column).toString.contains(criteria))
  }
}

object EventStore2 {
  type WebElements = Vector[Vector[net.ruippeixotog.scalascraper.model.Element]]
  var eventsOld: WebElements = Vector()
  var eventsNew: WebElements = Vector()
}

class Monitor2(url: String) {
  type WebElements = Vector[Vector[net.ruippeixotog.scalascraper.model.Element]]

  def updateEvents(events: WebElements): Unit = {
    EventStore2.eventsOld = EventStore2.eventsNew
    val scraper = new Scraper2(this.url)
    val allEvents = scraper.getCurrentEvents()
    EventStore2.eventsNew = scraper.filterEvents(allEvents, "Unterfranken", 4)
  }

  def hasChanged: Boolean = EventStore2.eventsOld.size != EventStore2.eventsNew.size

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
    val request = basicRequest.get(uri"https://api.telegram.org/bot${accessToken}/getUpdates")
    implicit val backend = HttpURLConnectionBackend()
    val response = request.send()

    println(response.body)
  }

}
