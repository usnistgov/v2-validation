package hl7.v2.profile

case class Range(min: Int, max: String) {
  assert( min >= 0, s"Range.min must be greater or equal to zero # $this" )
  assert( "*" == max || max.forall( _.isDigit ), s"Invalid Range.max format # $this" )
  assert( if( "*" == max ) true else max.toInt >= min, 
      s"Range.min must lower than Range.max # $this" )
  override def toString = s"$min..$max"
}
