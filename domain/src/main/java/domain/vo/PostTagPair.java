package domain.vo;

public record PostTagPair(String url, String tagName) {

	public static PostTagPair of(String url, String tagName) {
		return new PostTagPair(url, tagName);
	}
}
