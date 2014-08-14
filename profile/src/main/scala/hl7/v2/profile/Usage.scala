package hl7.v2.profile

sealed trait Usage

object Usage {

  case object R  extends Usage { override def toString = "R"  }
  case object RE extends Usage { override def toString = "RE" }
  case object C  extends Usage { override def toString = "C"  }
  case object X  extends Usage { override def toString = "X"  }
  case object O  extends Usage { override def toString = "O"  }
  case object B  extends Usage { override def toString = "B"  }
  case object W  extends Usage { override def toString = "W"  }

  def fromString( s: String ): Usage = s match {
    case "O"  => O
    case "R"  => R
    case "C"  => C
    case "B"  => B
    case "W"  => W
    case "X"  => X
    case "RE" => RE
    case  _   => throw new Error(s"Invalid usage '${s}'")
  }
}
