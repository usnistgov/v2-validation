package expression

import hl7.v2.profile.{BindingLocation, BindingStrength, Range, ValueSetSpec}
import nu.xom.Element
import nist.xml.util.XOMExtensions._
import hl7.v2.instance.{EscapeSeqHandler, Number, Separators, Text}

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object XMLDeserializer extends EscapeSeqHandler {

  val FAIL = "FAIL"
  val INCONCLUSIVE = "INCONCLUSIVE"
  val PASS = "PASS"

  /**
    * Creates an expression from a nu.xom.Element representing an assertion
    */
  //def assertion( e: Element ): Expression = expression( e.getChildElements().get(0) )

  /**
    * Creates an expression from a nu.xom.Element representing the expression
    */
  def expression( e: Element ): Expression = e.getLocalName match {
    case "Presence"    => presence( e )
    case "PathValue"   => pathValue( e )
    case "ComplexPathValue"   => complexPathValue( e )
    case "PlainText"   => plainText( e )
    case "Format"      => format( e )
    case "NumberList"  => numberList( e )
    case "StringList"  => stringList( e )
    case "SimpleValue" => simpleValue( e )
    case "SubContext"  => subContext( e )
    case "NOT"         => not( e )
    case "AND"         => and( e )
    case "OR"          => or( e )
    case "XOR"         => xor( e )
    case "IMPLY"       => imply( e )
    case "FORALL"      => forall( e )
    case "EXIST"       => exist( e )
    case "SetID"       => setId( e )
    case "IZSetID"     => IZsetId( e )
    case "Plugin"      => plugin( e )
    case "ValueSet"    => valueSet( e )
    case "isNULL"      => isNull( e )
    case "PlainCoConstraint" => plainCo( e )
    case "StringFormat" => stringFormat( e )
    case _ => throw new Error(s"[Error] Unknown expression node $e")
  } 

  // Generic Expressions
  private def presence( e: Element ): Presence = Presence( e.attribute("Path") )

  private def pathValue( e: Element ): PathValue = {
    val path1 = e.attribute("Path1")
    val path2 = e.attribute("Path2")
    val op    = operator( e.attribute("Operator") )
    val path1Mode = e.attribute("Path1Mode") match {
      case ""|null => MultiCompareMode.All()
      case mode => pathMode(mode)
    }
    val path2Mode = e.attribute("Path2Mode") match {
      case ""|null => MultiCompareMode.All()
      case mode => pathMode(mode)
    }
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    PathValue( path1, op, path2, getComparisonMode(e), npb, path1Mode, path2Mode )
  }

  private def complexPathValue( e: Element ): ComplexPathValue = {
    val path1 = e.attribute("Path1")
    val path2 = e.attribute("Path2")
    val strict = toBooleanWithDefault(e.attribute("Strict"), default = true)
    val op    = operator( e.attribute("Operator") )
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    ComplexPathValue( path1, op, path2, strict, getComparisonMode(e), npb )
  }

  // Value Expressions
  private def plainText( e: Element ): PlainText = {
    val path = e.attribute("Path")
    val text = e.attribute("Text")
    val ignoreCase = toBoolean( e.attribute("IgnoreCase") )
    val atLeastOnce = getAtLeastOnce(e)
    val range = getRange(e)
    atLeastOnceAndRangeCheck(e, range)
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    PlainText( path , text, ignoreCase, atLeastOnce, npb, range)
  }
  
    // Value Expressions
  private def plainCo( e: Element ): PlainText = {
    val path = e.attribute("KeyPath")
    val text = e.attribute("KeyValue")
    val ignoreCase = if (e.attribute("IgnoreCase") != "") toBoolean( e.attribute("IgnoreCase") ) else true
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    val atLeastOnce = getAtLeastOnce(e)
    val range = getRange(e)
    atLeastOnceAndRangeCheck(e, range)
    PlainText( path , text, ignoreCase, atLeastOnce, npb, range)
  }

  private def format( e: Element ): Format = {
    val path = e.attribute("Path")
    val regex = e.attribute("Regex")
    val atLeastOnce = getAtLeastOnce(e)
    val range = getRange(e)
    atLeastOnceAndRangeCheck(e, range)
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    Format( path, regex, atLeastOnce, npb, range)
  }

  private def numberList( e: Element ): NumberList = {
    val path = e.attribute("Path")
    val csv  = e.attribute("CSV").split(',').toList map ( _.toDouble )
    val atLeastOnce = getAtLeastOnce(e)
    val range = getRange(e)
    atLeastOnceAndRangeCheck(e, range)
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    NumberList( path, csv, atLeastOnce, npb, range)
  }

  private def stringList( e: Element ): StringList = {
    val path = e.attribute("Path")
    val csv  = e.attribute("CSV").split(',').toList //No need to trim since no spaces in the schema
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    val atLeastOnce = getAtLeastOnce(e)
    val range = getRange(e)
    atLeastOnceAndRangeCheck(e, range)
    StringList( path, csv, atLeastOnce, npb, range)
  }

  private def simpleValue( e: Element ): SimpleValue = {
    val path = e.attribute("Path")
    val op   = operator( e.attribute("Operator") )
    val _value = value( e.attribute("Value"), e.attribute("Type") )
    val atLeastOnce = getAtLeastOnce(e)
    val range = getRange(e)
    atLeastOnceAndRangeCheck(e, range)
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    SimpleValue( path, op, _value, getComparisonMode(e), atLeastOnce, npb, range)
  }

  private def subContext( e: Element ): SubContext = {
    val path = e.attribute("Path")
    val atLeastOnce = if (e.attribute("AtLeastOnce") != "") Some(toBoolean(e.attribute("AtLeastOnce"))) else None
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    if(e.getChildElements.size() != 1) {
      throw new Exception(s"SubContext element should have one and only one assertion, found = ${e.getChildElements.size()}")
    }
    val assertion = expression(e.getChildElements().get(0))
    val card = cardinality(e)
    SubContext(assertion, path, card, atLeastOnce, npb)
  }

  // Combination Expressions
  private def not( e: Element ) = NOT( expression( e.getChildElements.get(0) ) )

  private def and( e: Element ) = AND.tupled( combination2(e) )

  private def or( e: Element )  = OR.tupled( combination2(e) )

  private def xor( e: Element ) = XOR.tupled( combination2(e) )

  private def imply( e: Element )  = IMPLY.tupled( combination2(e) )

  private def forall( e: Element ) = FORALL( combinationn(e): _ * )

  private def exist( e: Element )  = EXIST( combinationn(e): _ * )

  private def setId( e: Element ) = SetId( e.attribute("Path") )
  
  private def IZsetId( e: Element ) = IZSetId( e.attribute("Parent"), e.attribute("Element"))

  private def plugin(e: Element) = Plugin( e.attribute("QualifiedClassName") )

  private def valueSet(e: Element) = {
    val id   = e.attribute("ValueSetID")
    val bs   = BindingStrength( e.attribute("BindingStrength") ).getOrElse(BindingStrength.R)
    val bl   = BindingLocation( e.attribute("BindingLocation") ).getOrElse(BindingLocation("1").get)
    val spec = ValueSetSpec( id, Some(bs), Some(bl) )
    val path = e.attribute("Path")
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    ValueSet(path, spec, npb)
  }
  
  private def stringFormat(e: Element) = {
    val path = e.attribute("Path")
    val format = e.attribute("Format")
    val atLeastOnce = getAtLeastOnce(e)
    val range = getRange(e)
    atLeastOnceAndRangeCheck(e, range)
    val npb = notPresentBehavior(e.attribute("NotPresentBehavior")).getOrElse(PASS)
    StringFormat(path, format, atLeastOnce, npb, range)
  }

  private def isNull(e : Element) = isNULL( e.attribute("Path") )
  
  // Helpers
  private def notPresentBehavior(value: String): Option[String] = {
    value match {
      case null => None
      case "" => None
      case _ =>
        val CODE = value.toUpperCase()
        CODE match {
          case FAIL | INCONCLUSIVE | PASS => Some(CODE)
          case _ => throw new Error(s"[Error] Invalid NotPresentBehavior value $value, expected : $FAIL, $INCONCLUSIVE, $PASS")
        }
    }
  }
  private def toBoolean( s: String ) = 
    if( "true" == s || "1" == s ) true
    else if( "false" == s || "0" == s || "" == s ) false
    else throw new Error(s"[Error] Invalid XSD:Boolean value $s")

  private def toBooleanWithDefault( s: String, default: Boolean ) =
    if( "true" == s || "1" == s ) true
    else if( "false" == s || "0" == s ) false
    else if( "" == s ) default
    else throw new Error(s"[Error] Invalid XSD:Boolean value $s")

  private def operator( v: String ) = v match {
    case "EQ" => Operator.EQ
    case "NE" => Operator.NE
    case "GT" => Operator.GT
    case "LT" => Operator.LT
    case "GE" => Operator.GE
    case "LE" => Operator.LE
    case   _  => throw new Error(s"[Error] Invalid Operator $v")
  }

  private def pathMode( v: String ) = v match {
    case "All" => MultiCompareMode.All()
    case "AtLeastOne" => MultiCompareMode.AtLeastOne()
    case x => try {
      val n = x.toInt
      if(n >= 0) MultiCompareMode.Count(n)
      else throw new Error(s"[Error] value '$x' is not a valid path comparison mode (must be >= 0)")
    } catch {
      case _: NumberFormatException  => throw new Error(s"[Error] value '$x' is not a recognized path comparison mode")
    }
  }

  private def getComparisonMode(e: Element): ComparisonMode = {
    val identicalEquality = e.attribute("IdenticalEquality")
    val truncated = e.attribute("Truncated")


    ComparisonMode(
      identicalEquality match {
        case "" | null => false
        case value => toBoolean(value)
      },
      truncated match {
        case "" | null => false
        case value => toBoolean(value)
      }
    )
  }

  private def getRange(e: Element): Option[Range] =
    asOption(e.attribute("Min")) map { min =>
      Range(min.toInt, e.attribute("Max"))
    }

  private def getAtLeastOnce(e: Element): Boolean = if (e.attribute("AtLeastOnce") != "") toBoolean(e.attribute("AtLeastOnce")) else false

  private def atLeastOnceAndRangeCheck(e: Element, range: Option[Range]): Unit = {
    range match {
      case None => ()
      case Some(_) =>
        if (e.attribute("AtLeastOnce") != "")
          throw new Error("AtLeastOnce and Min Max were defined, only one occurrences specification permitted, use eiter AtLeastOnce or (Min & Max)")
    }
  }



  implicit val separators = Separators( '|', '^', '~', '\\', '&', Some('#') )

  //FIXME This may not work properly because of DataElement.isUnescaped ...
  //We are assuming here that truncation is supported (see implicit val separators ) ...
  private def value( v: String, typ: String ) =
    if( "" == typ || "String" == typ ) Text( unescape(v) ) //FIXME: we will need to enforce the text format no separators should be allowed in XML or any inout file
    else if( "Number" == typ ) Number(v) //FIXME: we will need to enforce the number format in XML or any inout file
    else throw new Error(s"[Error] Unsupported value $v")

  private def combination2( e: Element ) = {
    val childElements = e.getChildElements
    val exp1 = expression( childElements.get(0) )
    val exp2 = expression( childElements.get(1) )
    ( exp1, exp2)
  }

  def cardinality(e: Element): Option[Range] =
    asOption(e.attribute("MinOccurrence")) map { min =>
    Range(min.toInt, e.attribute("MaxOccurrence"))
  }

  private def asOption(s: String) = if (s == "") None else Some(s)


  /*private def combinationn( e: Element ) = {
    val childElements = e.getChildElements
    ( 0 until childElements.size ) map ( i => expression( childElements.get( i ) ) )
  }*/

  private def combinationn( e: Element ) = e.getChildElements map expression
}