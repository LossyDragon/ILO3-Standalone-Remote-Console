import com.hp.ilo2.intgapp.intgapp
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import model.LoginResponse
import util.Http
import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import java.util.*
import javax.net.ssl.*
import kotlin.system.exitProcess

/*
 * ILO jar can be acquired from the jnlp file when clicking on the Java Web Start button.
 *      -> https://<Your-ILO3-Device>/html/intgapp3_231.jar
 *
 * For some reason, Java applet does not work (ILO 1.94 and FF 91.8.0esr)
 *
 * This program mimics https://github.com/scrapes/ILO2-Standalone-Remote-Console/ closely since ILO3 is basically
 * ILO2, but with json requests/responses
 *
 * Notes:
 *      Regex find and replace: getLocalString\((.*?)\) -> $1
 */

/** Global Variables **/
private val USAGE_TEXT: String = """
    Usage: 
    - ILO3RemCon.jar <Hostname or IP> <Username> <Password>
    - ILO3RemCon.jar -c <Path to config.properties>
    """.trimIndent()

private const val DEFAULT_CONFIG_PATH = "config.properties"

/** Main Variables **/
private var username = ""
private var password = ""
private var hostname = ""

fun main(args: Array<String>) {
    var config = Optional.empty<String>()

    // println("Args size: ${args.size}")
    // 0: No Args, try default properties
    // 2: -c ./config.properties
    // 3: <IP> <Username> <Password>
    when (args.size) {
        0 -> config = Optional.of(DEFAULT_CONFIG_PATH)
        2 -> {
            if (args[0] == "-c") {
                config = Optional.of(args[1])
            } else {
                println(USAGE_TEXT)
                return
            }
        }
        3 -> {
            hostname = args[0]
            username = args[1]
            password = args[2]
        }
        else -> {
            println(USAGE_TEXT)
            return
        }
    }

    if (config.isPresent) {
        try {
            FileInputStream(config.get()).use {
                Properties().run {
                    load(it)
                    hostname = getProperty("hostname").orEmpty()
                    username = getProperty("username").orEmpty()
                    password = getProperty("password").orEmpty()
                }
            }
        } catch (e: Exception) {
            System.err.println("Error reading config file")
            e.printStackTrace()
            return
        }
    }

    if(hostname.isBlank()|| username.isBlank() || password.isBlank())  {
        println("hostname, username, or password is blank!")
        exitProcess(1)
    }

    Http.sessionKey = doAuthentication(hostname, username, password)
        ?: throw java.lang.NullPointerException("SessionKey shouldn't have been null!")

    // Everything should be OK now. Connect to the applet.
    val intgapp = intgapp(hostname)
    intgapp.init()
    intgapp.start()
}

private fun doAuthentication(hostname: String, username: String, password: String): String? {

    // Connect to the ILO device
    val httpsURLConnection = URL(Http.getLoginUrl(hostname)).openConnection() as HttpsURLConnection
    httpsURLConnection.apply {
        requestMethod = "POST"
        doOutput = true
        useCaches = false
        sslSocketFactory = Http.sslContext.socketFactory
        hostnameVerifier = HostnameVerifier { _: String?, _: SSLSession? -> true }
        outputStream.run {
            val input: ByteArray = Http.getLoginBody(username, password).toByteArray(Charsets.UTF_8)
            write(input, 0, input.size)
        }
        connect()
    }

    // Authenticate and parse response
    var jsonResponse: LoginResponse? = null
    try {
        val response = httpsURLConnection.inputStream.reader().readText()
        println("response: $response")

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(LoginResponse::class.java)
        jsonResponse = adapter.fromJson(response)
    } catch (e: IOException) {
        // Sometimes it's finicky. Not sure why.
        e.printStackTrace()
        if (e.message != null && e.message!!.contains("Handshake.msg_type")) {
            println("Got some kind of handshake error. Try again!")
            exitProcess(1)
        }
    }

    if (jsonResponse == null) {
        throw java.lang.NullPointerException("Login response null")
    }

    println("jsonResponse: $jsonResponse")
    if (!jsonResponse.message.isNullOrEmpty()) {
        println("\nError:\nMessage:${jsonResponse.message}\nDetails:${jsonResponse.details}")
        exitProcess(1)
    }

    // Return the session key.
    return jsonResponse.sessionKey
}