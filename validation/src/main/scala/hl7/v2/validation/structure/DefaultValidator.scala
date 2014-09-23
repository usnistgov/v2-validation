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

/**
  * Default implementation of the structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultValidator extends Validator with BasicChecks {

  def checkStructure(m: Message): Future[Seq[Entry]] = Future {
    (m.unexpected, m.invalid) match {
      case (Nil, Nil) => check(m.asGroup)
      case (u, Nil)   => UnexpectedLines(u) :: check(m.asGroup)
      case (Nil, i)   => InvalidLines(i) :: check(m.asGroup)
      case (u, i)     => InvalidLines(i) :: UnexpectedLines(u) :: check(m.asGroup)
    }
  }

  /**
    * Checks the group against the constraints defined
    * in the profile and return the list of problem.
    */
  private def check(g: Group): List[Entry] =
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
  private def check(s: Segment): List[Entry] =
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
  private def check(f: Field): List[Entry] = f match {
    case sf: SimpleField  => check(sf, sf.model.length)
    case cf: ComplexField => check(cf.location, cf.components, cfc(cf))
  }

  /**
    * Checks the component against the constraints defined
    * in the profile and return the list of problems.
    */
  private def check(c: Component): List[Entry] = c match {
    case sc: SimpleComponent  => check(sc, sc.model.length)
    case cc: ComplexComponent => check(cc.location, cc.components, ccc(cc))
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
  private def check(l: Location, cl: List[OC], ml: List[CM]): List[Entry] =
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
    * @return The list of problems
    */
  private def check(s: Simple, l: Range): List[Entry] = checkLength(s, l)

  /**
    * Creates a new location by changing the path
    */
  private def location(l: Location, position: Int) =
    l.copy( path = s"${l.path}.$position")

  /**
    * Complex field children models
    */
  private def cfc(cf: ComplexField) = cf.model.datatype.components

  /**
    * Complex component children models
    */
  private def ccc(cc: ComplexComponent) = cc.model.datatype.components
}
