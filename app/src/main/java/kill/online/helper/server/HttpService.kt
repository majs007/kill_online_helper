package kill.online.helper.server

import android.util.Log
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse

class HttpServer(port: Int = HTTP_PORT_SERVER) : NanoHTTPD(port) {
    private val gson: Gson = Gson()
    private var onReceivedMessageCallback: (msg: Message) -> MessageResponse = { MessageResponse() }

    companion object {
        const val HTTP_PORT_SERVER = 17654
        const val URI_MESSAGE = "/message"
    }

    override fun serve(session: IHTTPSession?): Response {
//        val body: String = session?.inputStream?.bufferedReader().use { it?.readText() ?: "" }
        val body = mutableMapOf<String?, String?>()
        session?.parseBody(body)
        //日志输出外部请求相关的日志信息
        Log.i(
            "http server",
            "session.uri = ${session?.uri} method = ${session?.method} header = ${session?.headers} params = ${session?.parameters} body = $body"
        )

        //响应get请求
        when (session?.method) {
            Method.GET -> {
                return newFixedLengthResponse(
                    Response.Status.SERVICE_UNAVAILABLE, "text/html", "service unavailable"
                )
            }

            Method.POST -> {
                return when (session.uri) {
                    URI_MESSAGE -> {
                        val msgResponse = onReceivedMessageCallback(
                            gson.fromJson(
                                body["postData"], Message::class.java
                            )
                        )
                        newFixedLengthResponse(
                            Response.Status.OK,
                            "application/json",
                            gson.toJson(msgResponse)
                        )
                    }

                    else -> newFixedLengthResponse(
                        Response.Status.NOT_FOUND, "text/html", "NOT FOUND"
                    )
                }
            }

            else -> {
                return newFixedLengthResponse(
                    Response.Status.SERVICE_UNAVAILABLE, "text/html", "service unavailable"
                )
            }
        }
    }

    fun onReceivedMessage(callback: (msg: Message) -> MessageResponse) {
        onReceivedMessageCallback = callback
    }
}