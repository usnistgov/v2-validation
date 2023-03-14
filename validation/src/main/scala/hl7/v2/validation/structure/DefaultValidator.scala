package hl7.v2.validation
package structure

import gov.nist.validation.report.Entry
import hl7.v2.instance._
import hl7.v2.profile
import hl7.v2.profile.{Range, Usage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import hl7.v2.validation.report.ConfigurableDetections

import scala.util.{Failure, Success}

/**
  * Default implementation of the structure validation
  */
trait DefaultValidator extends Validator with EscapeSeqHandler {

  /**
    * Checks the message structure and returns the list of problems.
    *
    * @param m - The message to be checked
    * @return  - The list of problems
    */
  def checkStructure(m: Message)(implicit Detections : ConfigurableDetections): Future[List[Entry]] = Future {
    implicit val s = m.separators
    invalid( m.invalid ) ::: unexpected(m.unexpected) ::: check(m.asGroup)
  }

  /**
    * Checks the element against the the specified requirements
    * and recursively check the children if applicable
    * @param e - The element to be checked
    * @return A list of problems found
    */
  private def check(e: Element)(implicit sep: Separators, Detections : ConfigurableDetections): List[Entry] =
    e match {
      case s: Simple  => s match {
        case f : SimpleField => check(s)
        case _ => check(s)
      }
      case c: Complex => c match {
        case nX: NULLComplexField =>  Nil
        case _ => check(c);
      }
      case u: UnresolvedField => if(u.isInstanceOf[UnresolvedField]) Detections.unresolvedField(u.datatype.referenceValue1.getOrElse(""), u.datatype.referenceValue2.getOrElse(""), u) :: Nil else Nil
    }

  /**
    * Checks the simple element against the specified requirements
    * @param s   - The simple element to be checked
    * @return A list of problems found
    */
  private def check(ss: Simple)(implicit s: Separators, Detections : ConfigurableDetections): List[Entry] =
    ValueValidation.check(ss)


  private def collectNoteAboutSelectedDynamicMapping(segInstance: Segment)(implicit s: Separators, Detections : ConfigurableDetections): List[Entry] = {
    segInstance.model.ref.mappings match {
      case Nil => Nil
      case mappings => mappings.foldRight(List[Entry]()) {
        (mapping, acc) => {
          val path = s"""${mapping.position}[*]"""
          val referenceValues = List(mapping.reference, mapping.secondReference).flatMap(referenceOption => {
            referenceOption.map(ref => s"""${ref}[1]""") match {
              case Some(referenceElementOnePath) => Query.queryAsSimple(segInstance, referenceElementOnePath) match {
                case Failure(_) => None
                case Success(value) => value.headOption
              }
              case None => None
            }
          })
            .map(refElm => s"""${refElm.location.uidPath} = ${refElm.value.raw}""")
            .mkString("(", ", ", ")")

          Query.query(segInstance, path) match {
            case Failure(_) => acc
            case Success(dynElems) =>

              val entries = dynElems.foldLeft(List[Entry]()) {
                (acc, elm) => elm match {
                  case sf: SimpleField => Detections.dynMappingNote(sf.location, sf.datatype.id, referenceValues) :: acc
                  case cf: ComplexField => Detections.dynMappingNote(cf.location, cf.datatype.id, referenceValues) :: acc
                  case _ => Nil
                }
              }

              entries ::: acc
          }
        }
      }
    }
  }

  /**
    * Checks the children of the complex element against their requirements
    * @param c - The complex element to be checked
    * @return A list of problems found
    */
  private def check(c: Complex)(implicit s: Separators, Detections : ConfigurableDetections): List[Entry] = {
    val dynMappingNotes = c match {
      case segInst: Segment => collectNoteAboutSelectedDynamicMapping(segInst)
      case _ => Nil
    }

    // Sort the children by position
    val map = c.children.groupBy( x => x.position )

    // Check every position defined in the model
    val entries = c.reqs.foldLeft( List[Entry]() ) { (acc, r) =>
      // Get the children at the current position (r.position)
      val children = map.getOrElse(r.position, Nil)

      //FIXME we are missing the description here ...
      //val dl = c.location.copy(desc=r.description,
      //  path=s"${c.location.path}.${r.position}[1]")
      val dl = Utils.defaultLocation(c, r)

      // Checks the usage
      checkUsage( r.usage, children )(dl) match {
        case Nil => // No usage error thus we can continue the validation
          
          // Check the cardinality
          if(dl.eType == EType.Field && positionHasNull(children)){
            if(children.length == 1) acc
            else {
              val lowR = children minBy ( e => e.instance )
              Detections.ncardinality(lowR.location, children.length) :: Nil
            }
          }
          else{
            val r1 = checkCardinality( children, r.cardinality )
            // Recursively check the children
            val r2 = children flatMap check
            r1 ::: r2 ::: acc
          }
           
        case xs  => xs ::: acc // Usage problem no further check is necessary
      }
    }

    // Check if complex has constant value requirement
    val constantValueSpecError = if(c.req.constantValue.isDefined) Detections.constantValueSpecError(c.location, c.req.constantValue.get)::Nil else Nil
    // Check for extra children
    val extra = if( c.hasExtra ) Detections.extra(c.location) :: Nil else Nil
    // Check if it's a complex O element
    val optionalPopulated = if(c.req.usage.equals(Usage.O) && c.children.nonEmpty) Detections.oUsageComplex(c.location) :: Nil else Nil

    // Merge results
    constantValueSpecError ::: extra ::: optionalPopulated ::: entries ::: dynMappingNotes
  }
  
  private def positionHasNull(l : List[Element]): Boolean = {
    def loop(ls : List[Element]) = {
      ls match {
        case Nil => false
        case a::xs => a match {
          case n: NULLComplexField => true
          case _ => positionHasNull(xs)
        }
      }
    }
    loop(l)
  }

  /**
    * Returns a list of report entries if the usage is:
    *     1) R and the list of elements is empty
    *     2) X and the list of elements is not empty
    *     3) W and the list of elements is not empty
    *
    * @param u  - The usage
    * @param l  - The list of elements
    * @param dl - The default location
    * @return A list of report entries
    */
  private def checkUsage(u: Usage, l: List[Element])(dl: Location)(implicit Detections : ConfigurableDetections): List[Entry] =
    (u, l) match {
      case (Usage.R,  Nil) => Detections.rusage(dl)  :: Nil
      case (Usage.RE, Nil) => Detections.reusage(dl) :: Nil
      case (Usage.X,  xs ) => xs map { e => Detections.xusage(e.location) }
      case (Usage.W,  xs ) => xs map { e => Detections.wusage(e.location) }
      case _               => Nil
    }

  /**
    * Returns a list of report entries for every element which instance
    * number is greater than the maximum range or a list with a single
    * element if the highest instance number is lower than the minimum range
    *
    * @param l     - The list of element
    * @param range - The cardinality range
    * @return A list of report entries
    */
  private def checkCardinality(l: List[Element], range: Range)(implicit Detections : ConfigurableDetections): List[Entry] =
    if( l.isEmpty ) Nil 
    else {
      // The only reason this is needed is because of field repetition
      val highestRep = l maxBy ( e => e.instance )
      val i = highestRep.instance
      if( i < range.min )
        Detections.cardinality(highestRep.location, range, i) :: Nil
      else
        l filter { e => range.isBefore( e.instance ) } map { e =>
          Detections.cardinality(e.location, range, e.instance)
        }
    }

  private
  def checkCardinality(l: List[Element], or: Option[Range])(implicit Detections : ConfigurableDetections) : List[Entry] =
    or match {
      case Some(r) => checkCardinality(l, r)
      case None    => Nil
    }

  private def invalid(xs: List[Line])(implicit Detections : ConfigurableDetections): List[Entry] = xs map { line =>
    Detections.invalid(line.number, line.content)
  }

  private def unexpected(xs: List[Line])(implicit Detections : ConfigurableDetections): List[Entry] =
    xs map { line =>
      Detections.unexpected(line.number, line.content)
    }
}
