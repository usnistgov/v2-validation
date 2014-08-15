package hl7.v2.validation.content

trait ConstraintManager

/*
import java.io.InputStream
import scala.util.Try
//import hl7.v2.instance.DataElement
//import hl7.v2.instance.Group
//import hl7.v2.instance.Segment
import generic.Element

class ConstraintManager(
    val groupByID:      Map[String, List[Constraint]],
    val groupByName:    Map[String, List[Constraint]],
    val segmentByID:    Map[String, List[Constraint]],
    val segmentByName:  Map[String, List[Constraint]],
    val datatypeByID:   Map[String, List[Constraint]],
    val datatypeByName: Map[String, List[Constraint]]
  ) {

  def constraintsFor( g: Group ): List[Constraint] = groupByName.getOrElse( qname(g), Nil )

  def constraintsFor( s: Segment ): List[Constraint] = 
    segmentByName.getOrElse( qname(s), Nil) ::: segmentByID.getOrElse( qid(s), Nil)

  def constraintsFor( d: DataElement ): List[Constraint] = 
    datatypeByName.getOrElse( qname(d), Nil) ::: datatypeByID.getOrElse( qid(d), Nil)

  private def qid(e: Element) = ???//e.queryable.id

  private def qname(e: Element) = ???//e.queryable.name
}
*/