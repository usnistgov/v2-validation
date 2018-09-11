package expression


trait StringFormatValidatorUtil {
  
  type StringFormatValidator = (String) => Boolean
  def validators : Map[StringType, StringFormatValidator] = Map()
  
  
  def validateStringFormat(str : String, _type : StringType) : Boolean = {
      val validator : StringFormatValidator = validators(_type)
      return validator(str)
  }
}