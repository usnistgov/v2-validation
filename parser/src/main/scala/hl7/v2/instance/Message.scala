package hl7.v2.instance

import hl7.v2.profile.{Message => MM, Req, Composite}

import scala.collection.mutable.ArrayBuffer
import scala.util.Failure

/**
  * Class representing a message
  */
case class Message(
    model: MM,
    children: List[SegOrGroup],
    invalid: List[Line],
    unexpected: List[Line],
    defaultTimeZone: Option[TimeZone],//FIXME: Get this from MSH.7 ?
    separators: Separators
) {

  lazy val asGroup = Group( model.asGroup, 1, children)

  lazy val location = asGroup.location

  override def toString(): String = {
    var message = ""
    var isFirst = true
    for(segOrGroup <- children) {
      message+=segOrGroupAsString(segOrGroup,isFirst)
      isFirst=false
    }
    message
  }

  def findFieldsInSegment(req: Req, fieldList: List[Field]) : ArrayBuffer[Field] = {
    var fields = ArrayBuffer[Field]()
    for(field <- fieldList){
      if(req==field.req){
        fields+=field
      }
    }
    fields
  }

  def segOrGroupAsString(segOrGroup: SegOrGroup, isFirstSegOrGroup: Boolean) : String = {
    if(segOrGroup.isInstanceOf[Group]){
      var res = ""
      for(subSegOrGroup <- (segOrGroup.asInstanceOf[Group]).children){
        res += segOrGroupAsString(subSegOrGroup,false)
      }
      res
    } else {
      val segment = segOrGroup.asInstanceOf[Segment]
      var res = segment.model.ref.name
      if(!isFirstSegOrGroup){
        res+=separators.fs
      }
      var isFirst = true
      for (field <- segment.model.ref.fields) {
        val fieldsInSegment = findFieldsInSegment(field.req,segment.children)
        if(fieldsInSegment.length>0){
          if(!isFirst) {
            res += separators.fs
          }
          var isFirstFieldInSegment = true
          for(fieldInSegment <- fieldsInSegment){
            if(!isFirstFieldInSegment){
              res += separators.rs
            }
            res += fieldAsString(fieldInSegment)
            isFirstFieldInSegment=false
          }
          isFirst=false
        } else {
          res += separators.fs
        }
      }
      while(res.endsWith(""+separators.fs)){
        res = res.substring(0,res.length-1)
      }
      if(!isFirstSegOrGroup){
        res+=separators.ts.get
      }
      res+"\n"
    }
  }

  def findSimpleComponentInField(req:Req,components:List[Component]):Option[Component]={
    for(component <- components){
      if(req==component.req){
        return Some(component)
      }
    }
    None
  }

  def fieldAsString(field: Field): String ={
    if(field.isInstanceOf[ComplexField]){
      val complexField = field.asInstanceOf[ComplexField]
      var res = ""
      var isFirst = true
      for(component <- complexField.datatype.components){

        var componentInField = findSimpleComponentInField(component.req,complexField.children)
        if(!componentInField.isEmpty){
          if(!isFirst){
            res+=separators.cs
          }
          res += componentAsString(componentInField.get)
          isFirst = false
        } else {
          //if(component.req.usage!="X") {
            res += separators.cs
          //}
        }
      }
      while(res.endsWith(""+separators.cs)){
        res = res.substring(0,res.length-1)
      }
      res
    } else {
      val simpleField = field.asInstanceOf[SimpleField]
      simpleField.value.raw
    }
  }

  def componentAsString(component: Component):String = {
    if(component.isInstanceOf[ComplexComponent]){
      var res = ""
      val complexComponent = component.asInstanceOf[ComplexComponent]
      var isFirst=true
      for(simpleComponent <- complexComponent.children){
        if(!isFirst){
          res += separators.ss
        }
        res += componentAsString(simpleComponent)
        isFirst = false
      }
      res
    } else {
      val simpleComponent = component.asInstanceOf[SimpleComponent]
      simpleComponent.value.raw
    }
  }

}
