package hl7.v2.validation.vs

import com.fasterxml.jackson.databind.ObjectMapper
import gov.nist.validation.report.Entry
import hl7.v2.instance.{Message => MI, Segment => SM, _}
import hl7.v2.parser.impl.DefaultParser
import hl7.v2.profile.{Component, Composite, Field, Primitive, Req, Segment, Usage, Group => GM, Message => MM, _}
import hl7.v2.validation.FeatureFlags
import hl7.v2.validation.structure.Helpers
import hl7.v2.validation.vs.external.client.{FailureResponseException, InvalidContentTypeException, NetworkException, ResponseParsingException, ValueSetValidationHttpClientDisableException}
import hl7.v2.validation.vs.external.model.{CodeSetQueryResult, ExternalCode}
import hl7.v2.validation.vs.factory.ValueSetFactory
import hl7.v2.validation.vs.factory.impl.java.{DefaultValueSetFactory, DefaultValueSetFactoryConfiguration}
import hl7.v2.validation.vs.{BindingLocation, InternalValueSet => ValueSet}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpUriRequest}
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.protocol.HttpContext
import org.apache.http.{Header, HttpEntity, HttpHeaders, StatusLine}
import org.mockito.ArgumentMatchers.any
import org.specs2.Specification
import org.specs2.matcher.MatchResult
import org.specs2.mock.Mockito.{mock, theStubbed}

import java.io.{ByteArrayInputStream, IOException}
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util

trait ExternalVsValidationSpec extends Specification
       with Helpers
       with DefaultParser {def is =
  s2"""
  External Value Set Validation
    HTTP Request Active
      Validation against an external value set shall perform REST call when active and return RVS for existing R code $extRVS
      Validation against an external value set shall perform REST call when active and return Code Not Found for not existing code $extCNF
      External Service Error
        Validation shall issue an External Value Set Service error when the response from external service is not 200 $ext400
        Validation shall issue an External Value Set Service error when the response from external service is not application/json $extContentType
        Validation shall issue an External Value Set Service error when the response from external service is not json object $extBadFormat
        Validation shall issue an External Value Set Service error when the response from external service is not available $extUnknownHost
    HTTP Request Inactive $extInactiveHTTP

  """
  implicit val separators: Separators = Separators( '|', '^', '~', '\\', '&', Some('#'))
  implicit val time: Option[TimeZone] = None
  override implicit val featureFlags: FeatureFlags = FeatureFlags()
  val vs1: ValueSet = ValueSet("vs1", None, None, List(Code("A", "des", CodeUsage.R, "SYS"),Code("B", "des", CodeUsage.P, "SYS"),Code("C", "des", CodeUsage.E, "SYS")))
  val vs2: ValueSet = ValueSet("vs2", None, None, List(Code("X", "des", CodeUsage.R, "SYS"),Code("Y", "des", CodeUsage.P, "SYS"),Code("Z", "des", CodeUsage.E, "SYS")))
  val vs3: ValueSet = ValueSet("vs3", None, None, List(Code("D", "des", CodeUsage.R, "SYS"),Code("E", "des", CodeUsage.P, "SYS"),Code("F", "des", CodeUsage.E, "SYS")))
  val ext: ExternalValueSet = ExternalValueSet("ext001", "https://external-vs.com/ext001", None, None)
  // ============================================ TEST CASES ===========================================

  val extRVS: MatchResult[List[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("ext001"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(),
      Map()
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val (client, _) = mockSuccess(response(
      id = "ext001",
      name = "External VS",
      codeMatchValue = "A",
      codes = Code("A", "des", CodeUsage.R, "SYS")::Nil
    ))
    val validator = createValidator(specification, Some(client))
    implicit val bindingsMap: BindingOverrideMap = new BindingOverrideMap()
    val result = validator.checkMsgElement(parsedMessage.asGroup, None)
    result must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.RVS(Code("A", "des", CodeUsage.R, "SYS"), "A", ext),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  val extCNF: MatchResult[List[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("ext001"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(),
      Map()
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val (client, _) = mockSuccess(response(
      id = "ext001",
      name = "External VS",
      codeMatchValue = "A",
      codes = Nil
    ))
    val validator = createValidator(specification, Some(client))
    implicit val bindingsMap: BindingOverrideMap = new BindingOverrideMap()
    val result = validator.checkMsgElement(parsedMessage.asGroup, None)
    result must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.CodeNotFound("A", Some("SYS"), ext::Nil),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  val ext400: MatchResult[List[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("ext001"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(),
      Map()
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val (client, _) = mockFailure(500, "Internal Server Error", "application/json", """{"message":"server error"}""")
    val validator = createValidator(specification, Some(client))
    implicit val bindingsMap: BindingOverrideMap = new BindingOverrideMap()
    val result = validator.checkMsgElement(parsedMessage.asGroup, None)
    val expectedException = new FailureResponseException(500, "server error")
    result must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.ExternalValueSetValidationSpecError("A", expectedException.getMessage, ext),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  val extContentType: MatchResult[List[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("ext001"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(),
      Map()
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val (client, _) = mockSuccess(response(
      id = "ext001",
      name = "External VS",
      codeMatchValue = "A",
      codes = Nil
    ), contentType = "text/plain")
    val expectedException = new InvalidContentTypeException(ContentType.TEXT_PLAIN)
    val validator = createValidator(specification, Some(client))
    implicit val bindingsMap: BindingOverrideMap = new BindingOverrideMap()
    val result = validator.checkMsgElement(parsedMessage.asGroup, None)
    result must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.ExternalValueSetValidationSpecError("A", expectedException.getMessage, ext),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  val extBadFormat: MatchResult[List[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("ext001"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(),
      Map()
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val (client, _) = mockSuccess("[]")
    val validator = createValidator(specification, Some(client))
    implicit val bindingsMap: BindingOverrideMap = new BindingOverrideMap()
    val result = validator.checkMsgElement(parsedMessage.asGroup, None)
    val expectedException = new ResponseParsingException()
    result must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.ExternalValueSetValidationSpecError("A", expectedException.getMessage, ext),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  val extUnknownHost: MatchResult[List[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("ext001"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(),
      Map()
    )
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val client = mockFailureException(new UnknownHostException())
    val validator = createValidator(specification, Some(client))
    implicit val bindingsMap: BindingOverrideMap = new BindingOverrideMap()
    val result = validator.checkMsgElement(parsedMessage.asGroup, None)
    val expectedException = new NetworkException("Unknown host", new Exception())
    result must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.ExternalValueSetValidationSpecError("A", expectedException.getMessage, ext),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  val extInactiveHTTP: MatchResult[List[Entry]] = {
    val specification: ValueSetSpecification = new DefaultValueSetSpecification(
      Map(),
      Map(),
      Map("MSG" -> List(
        ValueSetBinding(
          "1[*].1[*].1[*].1[*]",
          Some(BindingStrength.R),
          List("ext001"),
          List(BindingLocation("1[1]", Some("3[1]")))
        )
      )),
      Map(),
      Map(),
      Map()
    )
    val exception = new ValueSetValidationHttpClientDisableException()
    val parsedMessage = parse("SEG|A&DESC&SYS"::Nil)
    val validator = createValidator(specification, None)
    implicit val bindingsMap: BindingOverrideMap = new BindingOverrideMap()
    val result = validator.checkMsgElement(parsedMessage.asGroup, None)
    result must containTheSameElementsAs(
      validator.createEntry(
        getOneElementByPath(parsedMessage, "1[*].1[*].1[*].1[*]"),
        VSValidationCode.ExternalValueSetValidationSpecError("A", exception.getMessage, ext),
        Some(BindingStrength.R)
      )::Nil
    )
  }

  // ============================================ MOCK ===========================================
  //  MSG / GRP / SEG (SEG) / FIELD (TWO) / COMP (CWE) / SUB-COMP (ST)
  def req(p: Int): Req = Req(p, "desc", Usage.R, None, None, None, Nil, None, hide = false, None)
  def seg_fields: Map[String, Field] = Map(
    "F1" -> Field("", TWO, req(1)),
  )
  def two_components: Map[String, Component] = Map(
    "C1" -> Component("C1", CWE, req(1))
  )
  def cwe_components: Map[String, Component] = Map(
    "C1" -> Component("C1", ST, req(1)),
    "C2" -> Component("C2", ST, req(2)),
    "C3" -> Component("C3", ST, req(3)),
  )
  def GroupRef: GM = GM("GRP", "GRP", List(SegRef), req(1))
  def MSG: MM = MM("MSG", "MSG", "MSG", "MSG", "MSG", List(GroupRef))
  def SegRef: SegmentRef = SegmentRef(req(1), SEG)
  def SEG: Segment = Segment("SEG", "SEG","", seg_fields.values.toList.sortBy(_.req.position), Nil)
  def CWE: Composite = Composite("CWE", "CWE", "Coded Element", "2.5", cwe_components.values.toList.sortBy(_.req.position))
  def TWO: Composite = Composite("TWO", "TWO", "Two Levels", "2.5", two_components.values.toList.sortBy(_.req.position))
  def ST: Primitive = Primitive("ST", "ST", "String", "2.5")
  def parse(values: List[String]): MI = {
    MI(MSG, List(Group(GroupRef, 1, None, segments(values))), Nil, Nil, time, separators)
  }
  def group(vs: List[String], group: GM): Group = Group(group, 1, None, segments(vs))
  def segments(vs: List[String]): List[SM] = {
    vs.zipWithIndex.map(v => {
      segment(v._1, v._2 + 1)
    })
  }
  def segment(v: String, i: Int): SM = {
    implicit val counter: Counter = Counter(scala.collection.mutable.Map[String, Int]())
    SM(SegRef, None, v, i, -1)
  }
  // ============================================ HELPERS ===========================================

  trait Default {
    val reqs: List[Req] = List[Req]()
    val location: Location = Location(null, "desc ...", "Path", -1, -1, "", None)
    val hasExtra = false
    val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None)
    val rawMessageValue = ""
  }

  case class S(override val position: Int, instance: Int, value: Value, override val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None))
    extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element], override val req: Req = Req(-1, "", Usage.O, None, None, None, Nil, None, hide = false, None))
    extends Complex with Default

  def createValidator(spec: ValueSetSpecification, client: Option[CloseableHttpClient]): DefaultValueSetValidator = {
    val valueSetLibrary = new ValueSetLibraryImpl(Nil, (vs1::vs2::vs3::ext::Nil).map(x => { (x.id, x) }).toMap)
    val factory: hl7.v2.validation.vs.factory.ValueSetFactory = new DefaultValueSetFactory(
      client.orNull,
      valueSetLibrary,
      new DefaultValueSetFactoryConfiguration(true, if (client.isDefined) true else false)
    )
    object validator extends DefaultValueSetValidator {
      override def vsSpecification: ValueSetSpecification = spec
      implicit val valueSetFactory: ValueSetFactory = factory
    }
    validator
  }

  def getElementByPath(message: MI, path: String): List[Element] = {
    Query.query(message.asGroup, path).getOrElse(Nil)
  }

  def getOneElementByPath(message: MI, path: String): Element = {
    Query.query(message.asGroup, path).getOrElse(Nil).head
  }

  def getOneElementByPathAsSimple(message: MI, path: String): Simple = {
    Query.queryAsSimple(message.asGroup, path).getOrElse(Nil).head
  }

  def mockSuccess(responseBody: String, contentType: String = "application/json"): (CloseableHttpClient, CloseableHttpResponse) = {
    val mockHttpClient = mock[CloseableHttpClient]
    val mockHttpResponse = mock[CloseableHttpResponse]
    val mockStatusLine = mock[StatusLine]
    val mockHttpEntity = mock[HttpEntity]
    val mockContentTypeHeader = mock[Header]

    val responseBodyBytes = responseBody.getBytes(StandardCharsets.UTF_8)
    val responseStream = new ByteArrayInputStream(responseBodyBytes)

    mockStatusLine.getStatusCode returns 200
    mockStatusLine.getReasonPhrase returns "OK"
    mockHttpResponse.getStatusLine returns mockStatusLine

    mockContentTypeHeader.getName returns HttpHeaders.CONTENT_TYPE
    mockContentTypeHeader.getValue returns contentType
    mockHttpEntity.getContentType returns mockContentTypeHeader

    mockHttpEntity.getContent returns responseStream
    mockHttpEntity.getContentLength returns responseBodyBytes.length.toLong
    mockHttpResponse.getEntity returns mockHttpEntity

    mockHttpClient.execute(any[HttpUriRequest], any[HttpContext]) returns mockHttpResponse

    (mockHttpClient, mockHttpResponse)
  }

  def mockFailure(statusCode: Int, reasonPhrase: String, contentType: String = "text/plain", errorBody: String = ""): (CloseableHttpClient, CloseableHttpResponse) = {
    val mockHttpClient = mock[CloseableHttpClient]
    val mockHttpResponse = mock[CloseableHttpResponse]
    val mockStatusLine = mock[StatusLine]
    val mockHttpEntity = mock[HttpEntity]
    val mockContentTypeHeader = mock[Header]

    val responseBodyBytes = errorBody.getBytes(StandardCharsets.UTF_8)
    val responseStream = new ByteArrayInputStream(responseBodyBytes)

    mockStatusLine.getStatusCode returns statusCode
    mockStatusLine.getReasonPhrase returns reasonPhrase
    mockHttpResponse.getStatusLine returns mockStatusLine

    mockContentTypeHeader.getName returns HttpHeaders.CONTENT_TYPE
    mockContentTypeHeader.getValue returns contentType
    mockHttpEntity.getContentType returns mockContentTypeHeader

    mockHttpEntity.getContent returns responseStream
    mockHttpEntity.getContentLength returns responseBodyBytes.length.toLong
    mockHttpResponse.getEntity returns mockHttpEntity

    mockHttpClient.execute(any[HttpUriRequest], any[HttpContext]) returns mockHttpResponse

    (mockHttpClient, mockHttpResponse)
  }

  def mockFailureException(exception: IOException): CloseableHttpClient = {
    val mockHttpClient = mock[CloseableHttpClient]
    mockHttpClient.execute(any[HttpUriRequest], any[HttpContext]) throws exception
    mockHttpClient
  }

  def response(
    id: String,
    name: String,
    codeMatchValue: String,
    codes: List[Code]
  ): String = {
    val result: CodeSetQueryResult = new CodeSetQueryResult()
    result.setId(id)
    result.setName(name)
    result.setCodeMatchValue(codeMatchValue)
    result.setCodes(new util.ArrayList[ExternalCode]())
    codes.foreach(code => {
      val ec = new ExternalCode()
      ec.setValue(code.value)
      ec.setUsage(code.usage.toString)
      ec.setDisplayText(code.description)
      ec.setCodeSystem(code.codeSys)
      result.getCodes.add(ec)
    })
    val mapper = new ObjectMapper()
    mapper.writeValueAsString(result)
  }
}