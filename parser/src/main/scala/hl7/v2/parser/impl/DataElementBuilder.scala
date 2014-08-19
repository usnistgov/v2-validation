package hl7.v2.parser.impl

import hl7.v2.instance.ComplexComponent
import hl7.v2.instance.ComplexField
import hl7.v2.instance.Component
import hl7.v2.instance.Field
import hl7.v2.instance.Location
import hl7.v2.instance.SimpleComponent
import hl7.v2.instance.SimpleField
import hl7.v2.instance.Value
import hl7.v2.profile.{Component => CM}
import hl7.v2.profile.{Field => FM}

/**
  * Data element (either a field or a component) builder
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object DataElementBuilder {

  /**
    * Creates and returns a field instance from a string value
    * 
    * @param m - The field model
    * @param v - The string value
    * @param l - The location
    * @return The field instance
    */
  def apply(m: FM, v: String, i: Int, l: Location): Option[Field] = 
    if( v matches emptyField ) None
    else m.datatype.components match {
      case Nil => Some( SimpleField(m, Value(m.datatype.name, v), i, l) )
      case fml => 
        val values   = split( cs, v, l.column )
        val children = fml map { mm => 
          val(col, vv) = get(values, mm.position - 1)
          if( vv matches emptyField ) None
          else apply(mm, vv, location(l, mm.position, col))
        }
        Some( ComplexField( m, children, i, l) )
    }

  /**
    * Creates and returns a component instance from a string value
    * 
    * @param m - The component model
    * @param v - The string value
    * @param l - The location
    * @return The component instance
    */
  def apply(m: CM, v: String, l: Location): Option[Component] = 
    if( v matches emptyComponent ) None
    else m.datatype.components match {
      case Nil => Some( SimpleComponent(m, Value(m.datatype.name, v), l) )
      case cml => 
        val values   = split( ss, v, l.column )
        val children = cml map { mm => 
          val(col, vv) = get(values, mm.position - 1)
          if( vv matches emptyComponent ) None
          else {
            val loc = location(l, mm.position, col)
            Some(SimpleComponent(mm, Value( mm.datatype.name, vv), loc) )
          }
        }
        Some( ComplexComponent( m, children, l) )
    }

  private val emptyComponent = s"(?:\\s*\\Q${cs}\\E*\\s*${ss}*\\s*)*"

  private val emptyField = s"(?:\\s*\\Q${cs}\\E*\\s*${ss}*\\s*)*"
}
