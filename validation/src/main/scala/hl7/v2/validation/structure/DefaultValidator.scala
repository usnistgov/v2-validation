package hl7.v2.validation.structure

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import hl7.v2.instance.ComplexComponent
import hl7.v2.instance.ComplexField
import hl7.v2.instance.Component
import hl7.v2.instance.Field
import hl7.v2.instance.Group
import hl7.v2.instance.Location
import hl7.v2.instance.Message
import hl7.v2.instance.Segment
import hl7.v2.instance.Simple
import hl7.v2.instance.SimpleComponent
import hl7.v2.instance.SimpleField
import hl7.v2.profile.{Component => CM}
import hl7.v2.profile.Range
import hl7.v2.validation.report.SEntry
  
/**
  * Default implementation of the structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultValidator extends Validator with BasicChecks {

  def checkStructure(m: Message): Future[Seq[SEntry]] = Future { check(m.asGroup) }

  /**
    * Checks the group against the constraints defined
    * in the profile and return the list of problem.
    */
  def check(g: Group): Seq[SEntry] = 
    (g.structure zip g.model.children) flatMap { _ match {
      case (Left(ls), Left(model)) => 
        val dl = location( g.location, model.position )
        checkUsage(model.usage, ls)(dl) match {
          case Nil => checkCardinality(ls, model.cardinality) ::: (ls flatMap check)
          case xs  => xs
        }
      case (Right(lg), Right(model)) => 
        val dl = location( g.location, model.position )
        checkUsage(model.usage, lg)(dl) match {
          case Nil => checkCardinality(lg, model.cardinality) ::: (lg flatMap check)
          case xs  => xs
        }
      case _ => ??? //FIXME
    }}

  /**
    * Checks the segment against the constraints defined
    * in the profile and return the list of problems.
    */
  def check(s: Segment): Seq[SEntry] = 
    (s.fields zip s.model.ref.fields) flatMap { t =>
      val(lf, model) = t
      val dl = location( s.location, model.position )
      checkUsage(model.usage, lf)(dl) match {
        case Nil => checkCardinality(lf, model.cardinality) ::: (lf flatMap check)
        case xs  => xs
      }
    }

  /**
    * Checks the component against the constraints defined
    * in the profile and return the list of problems.
    */
  def check(f: Field): Seq[SEntry] = f match {
    case sf: SimpleField  => checkSimpleElem(sf, sf.model.length, sf.model.table)
    case cf: ComplexField => checkComplexDataElem(cf.location, cf.components, childrenModels(cf))
  }

  /**
    * Checks the component against the constraints defined
    * in the profile and return the list of problems.
    */
  def check(c: Component): Seq[SEntry] = c match {
    case sc: SimpleComponent  => checkSimpleElem(sc, sc.model.length, sc.model.table)
    case cc: ComplexComponent => checkComplexDataElem(cc.location, cc.components, childrenModels(cc))
  }

  type OC = Option[Component] // Alias

  /**
    * Checks the complex data element (either a complex field or a 
    * complex component) children and returns the list of problems. 
    *
    * 
    * @param  l - The location of the complex data element
    * @param cl - The list of children
    * @param ml - The children models
    * @return The list of problems
    */
  private def checkComplexDataElem(l: Location, cl: List[OC], ml: List[CM]): Seq[SEntry] =
    (cl zip ml ) flatMap { t =>
      val(oc, model) = t
      val dl = location( l, model.position )
      checkUsage(model.usage, oc.toList)(dl) match {
        case Nil => oc match { case Some(c) => check(c) case _ => Nil }
        case xs  => xs
      }
    }

  /**
    * Checks the simple element and returns the list of problems
    * 
    * @param s - The simple element
    * @param l - The length constraint
    * @param t - The table constraint if any
    * @return The list of problems
    */
  private def checkSimpleElem(s: Simple, l: Range, t: Option[String]): Seq[SEntry] = 
    t match {
      case None    => checkLength(s, l)
      case Some(x) => checkLength(s, l) ::: checkTable(s, x)
    }

  /**
    * Creates a new location by changing the path
    */
  private def location(l: Location, position: Int) = l.copy( path = s"${l.path}.$position")

  /**
    * Complex field children models
    */
  private def childrenModels(cf: ComplexField) = cf.model.datatype.components

  /**
    * Complex component children models
    */
  private def childrenModels(cc: ComplexComponent) = cc.model.datatype.components
}
