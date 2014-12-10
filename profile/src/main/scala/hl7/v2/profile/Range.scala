package hl7.v2.profile

case class Range(min: Int, max: String) {
  assert( min >= 0, s"Range.min must be greater or equal to zero # $this" )
  assert( "*" == max || max.forall( _.isDigit ), s"Invalid Range.max format # $this" )
  assert( if( "*" == max ) true else max.toInt >= min, 
      s"Range.min must lower than Range.max # $this" )

  /**
    * Returns true if the range includes i
    */
  def includes(i: Int): Boolean = i >= min && (max == "*" || i <= max.toInt)

  /**
    * Returns if the range is before and not including i
    */
  def isBefore(i: Int): Boolean = if(max == "*") false else i > max.toInt

  override def toString = s"$min..$max"
}
