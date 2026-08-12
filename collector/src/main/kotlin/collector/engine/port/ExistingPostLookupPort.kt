package collector.engine.port

interface ExistingPostLookupPort {

    suspend fun findExistingUrls(urls: List<String>): Set<String>
}
