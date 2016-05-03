package hl7.v2.validation.content

import expression.EvalResult.{ Fail, Inconclusive, Pass, Trace, EvalData }
import gov.nist.validation.report.{ Entry, Trace => GTrace }
import hl7.v2.instance._
import hl7.v2.instance.Query._
import hl7.v2.validation.content.PredicateUsage.{ R, X }
import hl7.v2.validation.report.Detections
import hl7.v2.profile.{ Message => MM }
import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import expression._
import hl7.v2.validation.vs.ValueSetLibrary
import scala.util.{ Failure, Success, Try }
import scala.util.control.Breaks._

trait PatternFinder extends expression.Evaluator {
  
  implicit def valueSetLibrary: ValueSetLibrary
  
  def checkContexts(e: Element, c: List[Context], validator: (Element, Constraint) => Entry)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM): List[Entry] = {
    c.foldLeft(List[Entry]())({ (acc, context) =>
        checkContext(e,context,validator) ::: acc
    })
  }
  
  
  def checkContext(e: Element, c: Context, validator: (Element, Constraint) => Entry)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM): List[Entry] = {
     query(e, c.contextPath) match {
      case Success(Nil) => missingContext(c,e)
      case Success(x)   => checkPatterns(x, c.Patterns,e, validator)
      case Failure(err) => List[Entry](Detections.cntSpecError(e, Constraint("Content",None,None,err.getMessage,Presence(c.contextPath)), Nil))
    }
  }
  
  def missingContext(c : Context,e: Element): List[Entry] = {
      c.Patterns.foldLeft(List[Entry]())({ (acc, p) =>
        acc ::: List[Entry](Detections.HLcontentErr(p.trigger.errorMessage,e))
      })
  }
  
  def checkPatterns( el: List[Element], pl: List[Pattern], root: Element, validator: (Element, Constraint) => Entry) (implicit s: Separators,
    dtz: Option[TimeZone], model: MM): List[Entry] = {
    
    def loop( el: List[Element], pl: List[Pattern]): List[Entry] = {
      pl match {
        case x::tail => val z = search(x,el,root, validator); z._1 ++ loop(z._2,tail)
        case Nil  => Nil
      }
    }
    
    loop(el,pl)
  }
  
  def search(p : Pattern, el: List[Element], root: Element, validator: (Element, Constraint) => Entry)
  (implicit s: Separators, dtz: Option[TimeZone], model: MM) : (List[Entry], List[Element]) = {
    
    def find(els: List[Element], exp: Expression): List[Element] = {
      els match {
        case x::tail => eval(exp, x) match {
          case Pass => List[Element](x) ::: find(tail,exp)
          case  _   => find(tail,exp)
        }
        case Nil  => List[Element]()
      }
    }

    val _match = find(el,p.trigger.expression)
    
    if(_match.isEmpty) (List.fill(p.cardinality)(Detections.HLcontentErr(p.trigger.errorMessage, root)), el)
    else {
      val cstr_entries = _match map { checkConstraints(_,p.constraints,validator) }
      val cntx_entries = _match map { checkContexts(_,p.contexts,validator) }
      val missing      = List.fill(math.max(0, (p.cardinality - _match.size)))(Detections.HLcontentErr(p.trigger.errorMessage, root))
      (cstr_entries.flatten ::: cntx_entries.flatten ::: missing, el diff _match)
    }
  }
  
  def checkConstraints(e: Element,constraints: List[Constraint], validator: (Element, Constraint) => Entry)(implicit s: Separators,
    dtz: Option[TimeZone], model: MM): List[Entry] = {
   
    constraints.foldLeft(List[Entry]())({
      (acc, c) => acc ::: List[Entry](validator(e,c))
    });
    
  }
  
  
}