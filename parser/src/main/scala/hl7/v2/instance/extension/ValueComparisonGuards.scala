package hl7.v2.instance
package extension

/**
  * Extends the different value classes by adding isComparableWith
  * method which will returns true if the two value are comparable.
  */
object ValueComparisonGuards {

  implicit class TextComparisonGuard(val o: Text) extends AnyVal {
    def isComparableWith(v: Text)     = true
    def isComparableWith(v: Number)   = true
    def isComparableWith(v: Date)     = true
    def isComparableWith(v: Time)     = true
    def isComparableWith(v: DateTime) = true
    def isComparableWith(v: FText)    = true
  }

  implicit class FTextComparisonGuard(val o: FText) extends AnyVal {
    def isComparableWith(v: Text)     = true
    def isComparableWith(v: Number)   = true
    def isComparableWith(v: Date)     = true
    def isComparableWith(v: Time)     = true
    def isComparableWith(v: DateTime) = true
    def isComparableWith(v: FText)    = true
  }

  implicit class NumberComparisonGuard(val o: Number) extends AnyVal {
    def isComparableWith(v: FText)    = true
    def isComparableWith(v: Text)     = true
    def isComparableWith(v: Number)   = true
    def isComparableWith(v: Date)     = false
    def isComparableWith(v: Time)     = false
    def isComparableWith(v: DateTime) = false
  }

  implicit class DateComparisonGuard(val o: Date) extends AnyVal {
    def isComparableWith(v: FText)    = true
    def isComparableWith(v: Text)     = true
    def isComparableWith(v: Date)     = true
    def isComparableWith(v: Number)   = false
    def isComparableWith(v: Time)     = false
    def isComparableWith(v: DateTime) = false
  }

  implicit class TimeComparisonGuard(val o: Time) extends AnyVal {
    def isComparableWith(v: FText)    = true
    def isComparableWith(v: Text)     = true
    def isComparableWith(v: Time)     = true
    def isComparableWith(v: Number)   = false
    def isComparableWith(v: Date)     = false
    def isComparableWith(v: DateTime) = false
  }

  implicit class DateTimeComparisonGuard(val o: DateTime) extends AnyVal {
    def isComparableWith(v: FText)    = true
    def isComparableWith(v: Text)     = true
    def isComparableWith(v: DateTime) = true
    def isComparableWith(v: Number)   = false
    def isComparableWith(v: Date)     = false
    def isComparableWith(v: Time)     = false
  }

}
