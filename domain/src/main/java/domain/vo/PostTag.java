package domain.vo;

public record PostTag(String url, String tagName) {

	public static PostTag of(String url, String tagName) {
		return new PostTag(url, tagName);
	}
}
