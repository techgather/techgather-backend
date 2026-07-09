package api.controller.dto.response

import api.service.dto.result.ClassifyPostsResult

data class ClassifyPostsResponse(
    val requested: Int,
    val found: Int,
    val classified: Int,
    val llmClassified: Int,
    val keywordClassified: Int,
    val onHold: Int,
    val missingPostIds: List<String>,
    val classifiedPostIds: List<String>,
    val onHoldPostIds: List<String>
) {
    companion object {
        fun from(result: ClassifyPostsResult): ClassifyPostsResponse {
            return ClassifyPostsResponse(
                requested = result.requested,
                found = result.found,
                classified = result.classifiedPostIds.size,
                llmClassified = result.llmClassifiedPostIds.size,
                keywordClassified = result.keywordClassifiedPostIds.size,
                onHold = result.onHoldPostIds.size,
                missingPostIds = result.missingPostIds.map(Long::toString),
                classifiedPostIds = result.classifiedPostIds.map(Long::toString),
                onHoldPostIds = result.onHoldPostIds.map(Long::toString)
            )
        }
    }
}
