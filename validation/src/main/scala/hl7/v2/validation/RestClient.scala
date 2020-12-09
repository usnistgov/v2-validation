package hl7.v2.validation

import hl7.v2.validation.vs.ExternalValueSet
import spray.json._
import javax.net.ssl._
import java.security.cert.X509Certificate

case class ValueSetMetadata(resourceType: String, id: String, version: String, name: String, title: String)
case class ValidationResult(resourceType: String, parameter: List[Parameter])
trait Parameter { val name: String }
case class BooleanParameter(name: String, valueBoolean: Boolean) extends Parameter
case class StringParameter(name: String, valueString: String) extends Parameter

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val valueSetMetadataFormat: RootJsonFormat[ValueSetMetadata] = jsonFormat5(ValueSetMetadata)
}

// Bypasses both client and server validation.
object TrustAll extends X509TrustManager {
  val getAcceptedIssuers = null
  def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
  def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
}

// Verifies all host names by simply returning true.
object VerifiesAllHostNames extends HostnameVerifier {
  def verify(s: String, sslSession: SSLSession) = true
}

object RestClient {

  val JSON_FORMAT = "_format=json"

  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def get(url: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET"): String = {
    import java.net.{URL, HttpURLConnection}

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, Array(TrustAll), new java.security.SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier(VerifiesAllHostNames)

    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close()
    content
  }

  def getValueSetMetadata(vs: ExternalValueSet): ValueSetMetadata = {
    import MyJsonProtocol._
    val URL = s"${vs.url}/${vs.source}/ValueSet/${vs.id}?${JSON_FORMAT}"
    get(URL).parseJson.convertTo[ValueSetMetadata]
  }

//  def getCodes(vs: ExternalValueSet, code: String): ValidationResult = {
//    import MyJsonProtocol._
//    val OPERATION = """$validate-code"""
//    val URL = s"${vs.url}/${vs.source}/ValueSet/${vs.id}/${OPERATION}?${JSON_FORMAT}&code=${code}"
//    val json = get(URL).parseJson
//
//    json.asJsObject().getFields("parameter").head.asJsObject.
//  }

}
