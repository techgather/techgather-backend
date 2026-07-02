package collector.adapter.fetcher

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.tls.TLSException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Component
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@Component
class HttpFetcher: Fetcher {

    // 피드+썸네일 등 전체 HTTP 요청 총량을 제한해 공유 CIO 엔진/대상 서버 과부하를 방지한다
    private val requestLimiter = Semaphore(20)

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

    override suspend fun fetch(url: String): String = requestLimiter.withPermit {
        try {
            val response: HttpResponse = client.get(url)
            response.bodyAsText()
        } catch (e: TLSException) {
            throw IllegalStateException("TLS handshake failed for url=$url", e)
        } catch (e: Exception) {
            throw IllegalStateException("HTTP fetch failed for url=$url", e)
        }
    }
}
