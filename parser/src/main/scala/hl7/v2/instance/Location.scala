package hl7.v2.instance

/**
  * Class representing the location of an element
  */
case class Location(
    eType: EType,
    desc: String,
    path: String,
    line: Int,
    column: Int,
    uidPath : String,
    elementProfilePath: Option[ElementProfilePath],
  ) {
  val prettyString = s"$eType $path ($desc)"

  lazy val profilePositionPath: String = elementProfilePath.map(_.profilePositionPath).getOrElse("")
  lazy val profilePath: String = elementProfilePath.map(_.profilePath).getOrElse("")
  lazy val instancePositionPath: String = elementProfilePath.map(_.instancePositionPath).getOrElse("")
  lazy val instancePath: String = elementProfilePath.map(_.instancePath).getOrElse("")
}

case class ElementProfilePath (
  name: Option[String],
  eType: EType,
  position: Int,
  instance: Option[Int],
  child: Option[ElementProfilePath],
) {
  lazy val profilePositionPath: String = reduce(
    None,
    Some(this),
    (parent, current) => {
      parent.map(p => s"$p.${current.position}").getOrElse(s"${current.position}")
    }
  )
  lazy val profilePath: String = reduce(
    None,
    Some(this),
    (parent, current) => {
      val separator = if (current.eType.equals(EType.Field)) "-" else "."
      parent.map(p => s"$p$separator${current.name.getOrElse(current.position)}").getOrElse(current.name.getOrElse(s"${current.position}"))
    }
  )
  lazy val instancePositionPath: String = reduce(
    None,
    Some(this),
    (parent, current) => {
      val instance = current.instance.map((i) => s"[${i}]").getOrElse("")
      val currentPath = s"${current.position}$instance"
      parent.map(p => s"$p.$currentPath").getOrElse(currentPath)
    }
  )
  lazy val instancePath: String = reduce(
    None,
    Some(this),
    (parent, current) => {
      val separator = if (current.eType.equals(EType.Field)) "-" else "."
      val instance = current.instance.map((i) => s"[${i}]").getOrElse("")
      val currentPath = s"${current.name.getOrElse(current.position)}$instance"
      parent.map(p => s"$p$separator$currentPath").getOrElse(currentPath)
    }
  )

  def append(leaf: ElementProfilePath): ElementProfilePath = {
    this.copy(child = Some(this.child.map(c => c.append(leaf)).getOrElse(leaf)))
  }

  private def reduce(value: Option[String], node: Option[ElementProfilePath], fn: (Option[String], ElementProfilePath) => String): String = {
    node.map(n => reduce(Some(fn(value, n)), n.child, fn)).getOrElse(value.getOrElse(""))
  }
}

