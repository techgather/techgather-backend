package collector.adapter.fetcher

interface Fetcher {
    suspend fun fetch(urls: List<String>): List<String>

    suspend fun fetch(url: String): String
}