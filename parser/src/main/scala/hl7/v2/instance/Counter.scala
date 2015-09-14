package hl7.v2.instance

/**
 * @author root
 */
case class Counter(ls : scala.collection.mutable.Map[String,Int]) {
  def countFor(seg : String) : Int = {
    ls.get(seg) match {
      case Some(x) => ls(seg) = (x+1); x + 1
      case None => ls(seg) = 1; 1
    }
  }
}