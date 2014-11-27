package hl7.v2.instance
package extension

import org.specs2.Specification

import ValueComparisonGuards._

class ValueComparisonGuardsSpec extends Specification { def is = s2"""

  Value Comparison Guards Specifications

    Text(...) isComparableWith Text(...)   should return true     $text_text
    Text(...) isComparableWith FText(...)  should return true     $text_ftext
    Text(...) isComparableWith Number(...) should return true     $text_number
    Text(...) isComparableWith Date(...)   should return true     $text_date
    Text(...) isComparableWith Time(...)   should return true     $text_time
    Text(...) isComparableWith DateTime(...) should return true   $text_dateTime

    FText(...) isComparableWith Text(...)   should return true    $ftext_text
    FText(...) isComparableWith FText(...)  should return true    $ftext_ftext
    FText(...) isComparableWith Number(...) should return true    $ftext_number
    FText(...) isComparableWith Date(...)   should return true    $ftext_date
    FText(...) isComparableWith Time(...)   should return true    $ftext_time
    FText(...) isComparableWith DateTime(...) should return true $ftext_dateTime

    Number(...) isComparableWith Text(...)   should return true    $number_text
    Number(...) isComparableWith FText(...)  should return true    $number_ftext
    Number(...) isComparableWith Number(...) should return true   $number_number
    Number(...) isComparableWith Date(...)   should return false   $number_date
    Number(...) isComparableWith Time(...)   should return false   $number_time
    Number(...) isComparableWith DateTime(...) should return false $number_dateTime

    Date(...) isComparableWith Text(...)   should return true      $date_text
    Date(...) isComparableWith FText(...)  should return true      $date_ftext
    Date(...) isComparableWith Date(...)   should return true      $date_date
    Date(...) isComparableWith Number(...) should return false     $date_number
    Date(...) isComparableWith Time(...)   should return false     $date_time
    Date(...) isComparableWith DateTime(...) should return false  $date_dateTime

    Time(...) isComparableWith Text(...)   should return true      $time_text
    Time(...) isComparableWith FText(...)  should return true      $time_ftext
    Time(...) isComparableWith Time(...)   should return true      $time_time
    Time(...) isComparableWith Date(...)   should return false     $time_date
    Time(...) isComparableWith Number(...) should return false     $time_number
    Time(...) isComparableWith DateTime(...) should return false $time_dateTime

    DateTime(...) isComparableWith Text(...)   should return true  $dateTime_text
    DateTime(...) isComparableWith FText(...)  should return true  $dateTime_ftext
    DateTime(...) isComparableWith Time(...)   should return false $dateTime_time
    DateTime(...) isComparableWith Date(...)   should return false $dateTime_date
    DateTime(...) isComparableWith Number(...) should return false $dateTime_number
    DateTime(...) isComparableWith DateTime(...) should return true $dateTime_dateTime

"""

  val dtz = Some(TimeZone("+0000"))

  val text     = Text("x")
  val ftext    = FText("x")
  val number   = Number("1")
  val date     = Date("2014")
  val time     = Time("00", dtz)
  val dateTime = DateTime("2014", dtz)

  def text_text     = text isComparableWith text     must beTrue
  def text_ftext    = text isComparableWith ftext    must beTrue
  def text_number   = text isComparableWith number   must beTrue
  def text_date     = text isComparableWith date     must beTrue
  def text_time     = text isComparableWith time     must beTrue
  def text_dateTime = text isComparableWith dateTime must beTrue

  def ftext_text     = ftext isComparableWith text     must beTrue
  def ftext_ftext    = ftext isComparableWith ftext    must beTrue
  def ftext_number   = ftext isComparableWith number   must beTrue
  def ftext_date     = ftext isComparableWith date     must beTrue
  def ftext_time     = ftext isComparableWith time     must beTrue
  def ftext_dateTime = ftext isComparableWith dateTime must beTrue

  def number_text     = number isComparableWith text     must beTrue
  def number_ftext    = number isComparableWith ftext    must beTrue
  def number_number   = number isComparableWith number   must beTrue
  def number_date     = number isComparableWith date     must beFalse
  def number_time     = number isComparableWith time     must beFalse
  def number_dateTime = number isComparableWith dateTime must beFalse

  def date_text     = date isComparableWith text     must beTrue
  def date_ftext    = date isComparableWith ftext    must beTrue
  def date_number   = date isComparableWith number   must beFalse
  def date_date     = date isComparableWith date     must beTrue
  def date_time     = date isComparableWith time     must beFalse
  def date_dateTime = date isComparableWith dateTime must beFalse

  def time_text     = time isComparableWith text     must beTrue
  def time_ftext    = time isComparableWith ftext    must beTrue
  def time_number   = time isComparableWith number   must beFalse
  def time_date     = time isComparableWith date     must beFalse
  def time_time     = time isComparableWith time     must beTrue
  def time_dateTime = time isComparableWith dateTime must beFalse

  def dateTime_text     = dateTime isComparableWith text     must beTrue
  def dateTime_ftext    = dateTime isComparableWith ftext    must beTrue
  def dateTime_number   = dateTime isComparableWith number   must beFalse
  def dateTime_date     = dateTime isComparableWith date     must beFalse
  def dateTime_time     = dateTime isComparableWith time     must beFalse
  def dateTime_dateTime = dateTime isComparableWith dateTime must beTrue
}
