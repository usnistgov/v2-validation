package expression
import hl7.v2.validation.utils.format.utils.LOINCFormat;
import hl7.v2.validation.utils.format.utils.SNOMEDFormat;


final case class UnknownStringFormatException(private val message: String = "", private val cause: Throwable = None.orNull) extends Exception(message, cause) 

sealed trait StringType {
  def validate(str : String) : Boolean
}


object StringType {
	
	case class LOINC() extends StringType { 
	  override def toString = "LOINC"  
	  override def validate(str : String) : Boolean = {
	    LOINCFormat.isValid(str)
	  }
	}
	
	case class SNOMED() extends StringType { 
	  override def toString = "SNOMED" 
	  override def validate(str : String) : Boolean = {
	    SNOMEDFormat.isValid(str)
	  }
	}
	
	def fromString( s: String ): StringType = s match {
	  case "LOINC"  => LOINC()
	  case "SNOMED"  => SNOMED()
	  case  _   => throw new UnknownStringFormatException(s"Unknown string format '${s}'")
	}
}
