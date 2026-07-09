package api.service.dto.result

data class ClassifyPostsResult(
    val requested: Int,
    val found: Int,
    val classifiedPostIds: List<Long>,
    val llmClassifiedPostIds: List<Long>,
    val keywordClassifiedPostIds: List<Long>,
    val onHoldPostIds: List<Long>,
    val missingPostIds: List<Long>
)
