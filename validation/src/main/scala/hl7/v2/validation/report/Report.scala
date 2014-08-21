package hl7.v2.validation.report

import hl7.v2.validation.structure.{Entry => SEntry}
import hl7.v2.validation.content.{Entry => CEntry}
import hl7.v2.validation.vs.{Entry => VSEntry}

case class Report(structure: Seq[SEntry], content: Seq[CEntry], vs: Seq[VSEntry])