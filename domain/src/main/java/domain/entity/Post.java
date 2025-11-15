package domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "post", indexes = {
	@Index(name = "idx_post_url", columnList = "url", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "url")
public class Post {

	@Id
	private Long postId;

	private String title;

	@Lob
	@Column(columnDefinition = "TEXT", unique = true)
	private String url;

	@Column(name = "pub_date")
	private LocalDateTime pubDate;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String description;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String thumbnail;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PostTag> postTags = new HashSet<>();

	public static Post create(Long postId,
							  String title,
							  String url,
							  LocalDateTime pubDate,
							  String description,
							  String thumbnail) {
		Post post = new Post();
		post.postId = postId;
		post.title = title;
		post.url = url;
		post.pubDate = pubDate;
		post.description = description;
		post.thumbnail = thumbnail;
		return post;
	}

}