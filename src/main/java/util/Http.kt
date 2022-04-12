package util

import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class Http {

    companion object {

        @JvmStatic
        var sessionKey: String? = ""

        @JvmField
        val sslContext: SSLContext = SSLContext.getInstance("TLS")

        private val trustAllCerts: Array<TrustManager> = arrayOf(
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
                override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
                override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
            }
        )

        init {
            sslContext.init(null, trustAllCerts, SecureRandom())

            val props: Properties = System.getProperties()
            props.setProperty("jdk.internal.httpclient.disableHostnameVerification", true.toString())
        }

        @JvmStatic
        private fun getUrlHostname(ip: String): String = "https://$ip"

        @JvmStatic
        fun getLoginUrl(hostname: String): String = "${getUrlHostname(hostname)}/json/login_session"

        @JvmStatic
        fun getLoginBody(username: String, password: String): String =
            "{\"method\":\"login\",\"user_login\":\"$username\",\"password\":\"$password\"}"

        @JvmStatic
        fun getJavaAppletUrl(hostname: String, sessionKey: String?): String =
            "${getUrlHostname(hostname)}/html/java_irc.html?sessionKey=${sessionKey.orEmpty()}"
    }
}