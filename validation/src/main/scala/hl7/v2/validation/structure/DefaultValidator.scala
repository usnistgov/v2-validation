package hl7.v2.validation.structure

/*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import generic.Location
import hl7.v2.instance.Composite
import hl7.v2.instance.DataElement
import hl7.v2.instance.Group
import hl7.v2.instance.Message
import hl7.v2.instance.Primitive
import hl7.v2.instance.Segment
import hl7.v2.validation.report.Entry
import hl7.v2.vs.{Validator => VSValidator}

/**
  * Default implementation of the structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultValidator extends Validator with BasicChecks {

  def checkStructure(m: Message): Future[Seq[Entry]] = Future { check( m.asGroup ) }

      /**
    * Checks the message and returns the list of structure errors if any
    */
  private[structure] def check(m: Message): Seq[Entry] = check(m.asGroup)

  /**
    * Checks the group and returns the list of structure errors if any
    */
  private[structure] def check(g: Group): Seq[Entry] = 
    zip3(g.structure, g.req) flatMap { t =>
      val(position, losg, req) = t
      val lsg = losg.merge
      val dl = location( g.location, position )
      checkUsage(req.usage, lsg)(dl) match {
        case Nil => 
          val childErrors = losg match { 
            case Left (ls) => ls flatMap check 
            case Right(lg) => lg flatMap check
          }
          checkCardinality(lsg, req.cardinality) ::: childErrors
        case xs  => xs
      }
    }

  /**
    * Checks the segment and returns the list of structure errors if any
    */
  private[structure] def check(s: Segment): Seq[Entry] = 
    zip3(s.fields, s.req) flatMap { t =>
      val(position, lf, req) = t
      val dl = location( s.location, position )
      checkUsage(req.usage, lf)(dl) match {
        case Nil => checkCardinality(lf, req.cardinality) ::: (lf flatMap check)
        case xs  => xs
      }
    }

  /**
    * Checks the data element and returns the list of structure errors if any
    */
  private[structure] def check(d: DataElement): Seq[Entry] = d match {
    case p: Primitive => check(p)
    case c: Composite => check(c)
  }

  /**
    * Checks the composite data element and returns the list of structure errors if any
    */
  private[structure] def check(c: Composite): Seq[Entry] = 
    zip3(c.components, c.req) flatMap { t =>
      val(position, oc, req) = t
      val dl = location( c.location, position )
      checkUsage(req.usage, oc.toList)(dl) match {
        case Nil => oc match { case Some(c) => check(c) case _ => Nil }
        case xs  => xs
      }
    }

  /**
    * Checks the primitive data element and returns the list of structure errors if any
    */
  private[structure] def check(p: Primitive): Seq[Entry] = 
    List( checkLength(p), checkTable(p) ).flatten

  private def checkLength(p: Primitive): Option[Entry] = checkLength(p, p.req.length)

  private def checkTable(p: Primitive): Option[Entry] = p.req.table flatMap( checkTable(p, _) )

  private def zip3[A,B](s1: Seq[A], s2: Seq[B]) = ( Stream.from(1), s1, s2).zipped.toList

  private def location(l: Location, position: Int) = l.copy( path = s"${l.path}.$position" ) 
}
*/