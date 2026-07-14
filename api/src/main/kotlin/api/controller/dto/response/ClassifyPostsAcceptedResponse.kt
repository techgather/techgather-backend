package api.controller.dto.response

data class ClassifyPostsAcceptedResponse(
    val status: String,
    val requested: Int,
    val postIds: List<String>
) {
    companion object {
        fun of(postIds: List<String>): ClassifyPostsAcceptedResponse {
            return ClassifyPostsAcceptedResponse(
                status = "ACCEPTED",
                requested = postIds.size,
                postIds = postIds
            )
        }
    }
}
