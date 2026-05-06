package domain.entity;

import domain.common.BaseTime;
import domain.constants.Language;
import domain.constants.PostStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "post", indexes = {
	@Index(name = "idx_post_url", columnList = "url", unique = true),
	@Index(name = "idx_post_source_site_name", columnList = "sourceSiteName"),
	@Index(name = "idx_post_status_source", columnList = "status, sourceSiteName"),
	@Index(name = "idx_post_status_pubdate_id", columnList = "status, pub_date, postId"),
	@Index(name = "idx_post_status_lang_pubdate_id", columnList = "status, language, pub_date, postId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTime {

	@Id
	private Long postId;

	private String title;

	@Column(length = 768, unique = true)
	private String url;

	@Column(name = "pub_date")
	private LocalDateTime pubDate;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String thumbnail;

	@Column(name = "sourceSiteName")
	private String sourceSiteName;

	@Column(name = "language")
	@Enumerated(EnumType.STRING)
	private Language language;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private PostStatus status;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PostTag> postTags = new HashSet<>();

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PostCategory> postCategories = new HashSet<>();

	public static Post create(Long postId,
							  String title,
							  String url,
							  LocalDateTime pubDate,
							  String thumbnail,
							  String sourceSiteName,
							  String language) {
		Post post = new Post();
		post.postId = postId;
		post.title = title;
		post.url = url;
		post.pubDate = pubDate;
		post.thumbnail = thumbnail;
		post.sourceSiteName = normalizeSourceSiteName(sourceSiteName);
		post.language = Language.fromCode(language);
		post.status = PostStatus.NOT_PUBLISHED;
		return post;
	}

	private static String normalizeSourceSiteName(String sourceSiteName) {
		if (sourceSiteName == null) {
			return null;
		}
		return sourceSiteName.trim().toLowerCase(Locale.ROOT);
	}

}
