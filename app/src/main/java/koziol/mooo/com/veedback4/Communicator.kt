package koziol.mooo.com.veedback4

import android.os.Environment
import androidx.compose.runtime.rememberCoroutineScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import koziol.mooo.com.veedback4.models.Problem
import java.io.File

class Communicator {
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun addProblem() {

        val response: HttpResponse = client.post("http://verbinden.mooo.com:6878/multipart") {

            var pic2 = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() +
                        "/" +
                        "IMG_20230427_141939.jpg"
            )



            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "problem",
                            "{\"id\": \"100\", \"color\": \"YELLOW\", \"floor\": \"GROUNDFLOOR\", \"locationX\": \"43\", \"locationY\": \"55\"}"
                        )
                        append("image", pic2.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                        })
                    }
                )
            )
            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }
    }

    suspend fun getProblems(): List<Problem> {

        var probs: List<Problem> = client.get("http://verbinden.mooo.com:6878/problem").body()
        probs.forEach {
            println(it.pictureUuid)
        }
        return probs
    }
}