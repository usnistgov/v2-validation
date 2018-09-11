package expression

sealed trait StringType

object StringType {
	
	case object LOINC extends StringType { override def toString = "LOINC"  }
	case object SNOMED extends StringType { override def toString = "SNOMED" }
	
	def fromString( s: String ): StringType = s match {
	  case "LOINC"  => LOINC
	  case "SNOMED"  => SNOMED
	  case  _   => throw new Error(s"Invalid usage '${s}'")
	}
}
