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

  def getDataFromMessage(toBeFound: List[String]): Map[String,String] = {
    var data = Map[String, String]()
    for(segOrGroup <- children){
      if(data.size<toBeFound.size){
        data = findDataInSegOrGroup(segOrGroup,data,toBeFound)
      }
    }
    data
  }

  def printString(toBeReplaced: Option[Map[String,String]] = None): String = {
    var message = ""
    var isFirst = true
    for(segOrGroup <- children) {
      message+=segOrGroupAsString(segOrGroup,isFirst,toBeReplaced)
      isFirst=false
    }
    if (message.endsWith("\n")) {
      message = message.substring(0,message.length-"\n".length)
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

  def segOrGroupAsString(segOrGroup: SegOrGroup, isFirstSegOrGroup: Boolean,toBeReplaced: Option[Map[String,String]] = None) : String = {
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
            res += fieldAsString(fieldInSegment,toBeReplaced)
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

  def fieldAsString(field: Field,toBeReplaced: Option[Map[String,String]] = None): String ={
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
          res += componentAsString(componentInField.get,toBeReplaced)
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
      if(!toBeReplaced.isEmpty){
        val map = toBeReplaced.get
        for(key <- map.keys){
          if(comparePath(key,simpleField.location.path)){
            return map.get(key).get
          }
          /*else if(simpleField.req.description.startsWith(key)){
            return map.get(key).get
          }*/
        }
      }
      simpleField.value.raw
    }
  }

  def comparePath(fieldName:String,path:String):Boolean={
    val formattedPath = path.replace('.','-')
    val formattedFileName = fieldName.filterNot(_ == '0')
    if(formattedPath.equals(formattedFileName))
      true
    else
      false
  }

  def componentAsString(component: Component,toBeReplaced: Option[Map[String,String]] = None):String = {
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
      if(!toBeReplaced.isEmpty){
        val map = toBeReplaced.get
        for(key <- map.keys){
          if(comparePath(key,simpleComponent.location.path)){
            return map.get(key).get
          }
          /*if(simpleComponent.req.description.startsWith(key)){
            return map.get(key).get
          }*/
        }
      }
      simpleComponent.value.raw
    }
  }

  def findDataInComponent(component: Component, inData: Map[String, String], toBeFound: List[String]): Map[String,String] = {
    var data = inData
    if(component.isInstanceOf[ComplexComponent]){
      val complexComponent = component.asInstanceOf[ComplexComponent]
      for(simpleComponent <- complexComponent.children){
        data = findDataInComponent(simpleComponent,data,toBeFound)
      }
    } else {
      val simpleComponent = component.asInstanceOf[SimpleComponent]
      for(fieldName <- toBeFound){
        if(comparePath(fieldName,simpleComponent.location.path)){
          data+=(fieldName->simpleComponent.value.raw)
        }
      }
    }
    data
  }

  def findDataInField(field: Field, inData: Map[String, String], toBeFound: List[String]): Map[String, String] = {
    var data = inData
    if(field.isInstanceOf[ComplexField]){
      val complexField = field.asInstanceOf[ComplexField]
      for(component <- complexField.datatype.components){
        var componentInField = findSimpleComponentInField(component.req,complexField.children)
        if(!componentInField.isEmpty) {
          data = findDataInComponent(componentInField.get, data,toBeFound)
        }
      }
    } else {
      val simpleField = field.asInstanceOf[SimpleField]
      for(fieldName <- toBeFound){
        if(comparePath(fieldName,simpleField.location.path)){
          data+=(fieldName->simpleField.value.raw)
        }
      }
    }
    data
  }

  def findDataInSegOrGroup(segOrGroup: SegOrGroup, inData: Map[String, String], toBeFound: List[String]): Map[String, String] = {
    var data = inData
    if(segOrGroup.isInstanceOf[Group]){
      for(subSegOrGroup <- (segOrGroup.asInstanceOf[Group]).children){
        data = findDataInSegOrGroup(subSegOrGroup,data,toBeFound)
      }
    } else {
      val segment = segOrGroup.asInstanceOf[Segment]
      for (field <- segment.model.ref.fields) {
        val fieldsInSegment = findFieldsInSegment(field.req,segment.children)
        if(fieldsInSegment.length>0){
          for(fieldInSegment <- fieldsInSegment) {
            data = findDataInField(fieldInSegment, data,toBeFound)
          }
        }
      }
    }
    data
  }

}
