package domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_tag", indexes = {
	@Index(name = "idx_post_tag_post_id", columnList = "post_id"),
	@Index(name = "idx_post_tag_tag_id", columnList = "tag_id")},
		uniqueConstraints = {
	@UniqueConstraint(name = "uk_post_tag_post_tag", columnNames = {"post_id", "tag_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

	@Id
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id", nullable = false)
	private Tag tag;

	public static PostTag create(Post post, Tag tag) {
		PostTag postTag = new PostTag();
		postTag.setPost(post);
		postTag.setTag(tag);
		return postTag;
	}

	private void setPost(Post post) {
		this.post = post;
	}

	private void setTag(Tag tag) {
		this.tag = tag;
	}

}

