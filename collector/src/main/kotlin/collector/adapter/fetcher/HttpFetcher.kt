package collector.adapter.fetcher

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@Component
class HttpFetcher: Fetcher {

    private val client = HttpClient(CIO) {
        expectSuccess = false
        engine {
            requestTimeout = 30000
            endpoint {
                connectTimeout = 30000
                socketTimeout = 30000
            }
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                }
            }
        }
    }

    override suspend fun fetch(urls: List<String>): List<String> = coroutineScope {
        urls.map { url ->
            async {
                fetch(url)
            }
        }.awaitAll()
    }

    override suspend fun fetch(url: String): String {
        val response: HttpResponse = client.get(url)
        return response.bodyAsText()
    }
}