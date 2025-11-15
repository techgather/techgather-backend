package domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "tag", indexes = {
	@Index(name = "idx_tag_name", columnList = "name", unique = true)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

	@Id
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PostTag> postTags = new HashSet<>();

	public static Tag create(String name) {
		Tag tag = new Tag();
		tag.name = name;
		return tag;
	}

	public static Tag create(Long id, String name) {
		Tag tag = new Tag();
		tag.id = id;
		tag.name = name;
		return tag;
	}

	public void addPost(Post post) {
		if (post == null) {
			return;
		}
		PostTag.create(post, this);
	}
}
