package hl7.v2.validation.structure

import org.specs2.Specification
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.validation.vs.ValueSetBinding
import hl7.v2.profile.BindingStrength
import hl7.v2.validation.vs.BindingLocation
import hl7.v2.validation.vs.DefaultValueSetSpecification
import hl7.v2.validation.vs.ValueSet
import hl7.v2.validation.vs.CodeUsage
import hl7.v2.validation.vs.Code
import hl7.v2.validation.vs.ValueSetLibraryImpl
import hl7.v2.profile.Req
import hl7.v2.instance.Location
import hl7.v2.profile.Usage
import hl7.v2.instance.Value
import hl7.v2.instance.Simple
import hl7.v2.instance.Element
import hl7.v2.instance.Complex
import hl7.v2.instance.Text
import hl7.v2.instance.Number


trait ValueSetValidationSpec extends Specification
       with hl7.v2.validation.vs.DefaultValueSetValidator
       with Helpers
       with DefaultParser {def is = s2"""

  ValueSet Specification

    xxxxxxxx             """+e1_vs1.toString()+"""

  """
  
  val simpleBinding = ValueSetBinding(
      "1[*]", 
      BindingStrength.R, 
      List("vs"), 
      List(BindingLocation(".", None)),
      false
  )
  val vsSpecification = new DefaultValueSetSpecification(Map(("x", simpleBinding :: Nil)))


  trait Default {
    val reqs     = List[Req]()
    val location = Location(null, "desc ...", "Path", -1, -1)
    /*val qProps   = QProps(QType.DT, "id", "name")*/
    val hasExtra = false
    val req = Req(-1, "", Usage.O, None, None, None, Nil)
  }

  case class S(override val position: Int, instance: Int, value: Value)
  extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element])
  extends Complex with Default
  
  
  //========= DATA MOCK ==========
  //___Value Set
  val vs1 = ValueSet("vs1", None, None, List(Code("P", "des", CodeUsage.P, "SYS"),Code("E", "des", CodeUsage.E, "SYS"),Code("R", "des", CodeUsage.R, "SYS")))
  val vs2 = ValueSet("vs2", None, None, List(Code("X", "des", CodeUsage.R, "SYS"),Code("X", "des", CodeUsage.R, "SYS_"),Code("Z", "des", CodeUsage.R, "SYS")))
  val vs3 = ValueSet("vs3", None, None, List(Code("A", "des", CodeUsage.P, "SYS"),Code("B", "des", CodeUsage.E, "SYS"),Code("C", "des", CodeUsage.R, "SYS")))
  val valueSetLibrary = new ValueSetLibraryImpl(Nil, (vs1::vs2::vs3::Nil).map(x => { (x.id, x) }).toMap)
      
  
  // Simple Binding
  val sb_one = ValueSetBinding(
      ".", 
      BindingStrength.R, 
      List("vs1"), 
      List(BindingLocation(".", None)),
      false
  )
  val sb_two = ValueSetBinding(
      ".", 
      BindingStrength.R, 
      List("vs1","vs2"), 
      List(BindingLocation(".", None)),
      false
  )
  
  // Code : P
  val e1_c = S(1,1,Text("P"))
  // One VS
  val e1_vs1 = check(e1_c, sb_one)
  // Two VS
  val e1_vs2 = check(e1_c, sb_two).map(_.getDescription)
  println(e1_vs2)
  
  // Code : E
  val e2_c = S(1,1,Text("E"))
  // One VS
  val e2_vs1 = check(e1_c, sb_one)
  // Two VS
  val e2_vs2 = check(e1_c, sb_two)
  
  // Code : R
  val e3_c = S(1,1,Text("R"))
  // One VS
  val e3_vs1 = check(e1_c, sb_one)
  // Two VS
  val e3_vs2 = check(e1_c, sb_two)
  
  // Code : U
  val e4_c = S(1,1,Text("U"))
  // One VS
  val e4_vs1 = check(e1_c, sb_one)
  // Two VS
  val e4_vs2 = check(e1_c, sb_two)
  
  
  
  
  
  
  
}