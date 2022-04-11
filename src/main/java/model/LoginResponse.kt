package model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "config_priv")
    val configPriv: Int? = null,
    @Json(name = "login_priv")
    val loginPriv: Int? = null,
    @Json(name = "remote_cons_priv")
    val remoteConsPriv: Int? = null,
    @Json(name = "reset_priv")
    val resetPriv: Int? = null,
    @Json(name = "session_key")
    val sessionKey: String? = null,
    @Json(name = "user_account")
    val userAccount: String? = null,
    @Json(name = "user_dn")
    val userDn: String? = null,
    @Json(name = "user_expires")
    val userExpires: String? = null,
    @Json(name = "user_ip")
    val userIp: String? = null,
    @Json(name = "user_name")
    val userName: String? = null,
    @Json(name = "user_priv")
    val userPriv: Int? = null,
    @Json(name = "user_type")
    val userType: String? = null,
    @Json(name = "virtual_media_priv")
    val virtualMediaPriv: Int? = null,

    /* Errors */
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "details")
    val details: String? = null
)
