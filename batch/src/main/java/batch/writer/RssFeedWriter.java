package batch.writer;

import application.generator.SnowFlake;
import batch.message.RssFeedMessage;
import domain.entity.Post;
import domain.entity.Tag;
import domain.repository.CustomPostRepository;
import domain.repository.CustomPostTagRepository;
import domain.repository.CustomTagRepository;
import domain.util.TagNormalizerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
public class RssFeedWriter implements ItemWriter<RssFeedMessage> {

	private final CustomPostRepository customPostRepository;
	private final CustomPostTagRepository customPostTagRepository;
	private final CustomTagRepository customTagRepository;
	private final SnowFlake snowflake = SnowFlake.getInstance();

	@Override
	public void write(Chunk<? extends RssFeedMessage> chunk) {
		if (chunk.isEmpty()) {
			return;
		}

		processItems(chunk);
	}

	private void processItems(Chunk<? extends RssFeedMessage> chunk) {
		List<RssFeedMessage> validItems = new ArrayList<>();

		for (var item : chunk) {
			validItems.add(item);
		}

		if (CollectionUtils.isEmpty(validItems)) {
			return;
		}

		processTagsAndPosts(validItems);
	}

	private void processTagsAndPosts(List<RssFeedMessage> items) {
		List<String> tagNames = extractTagNames(items);
		if (!CollectionUtils.isEmpty(tagNames)) {
			List<Tag> tags = tagNames.stream()
					.map(name -> Tag.create(snowflake.nextId(), name))
					.toList();
			customTagRepository.saveAllTag(tags);
		}

		List<Post> posts = convertToPosts(items);
		if (!CollectionUtils.isEmpty(posts)) {
			customPostRepository.saveAllPost(posts);
		}
		savePostTags(items);
	}

	private List<String> extractTagNames(List<RssFeedMessage> items) {
		TagNormalizerUtils normalizer = TagNormalizerUtils.getInstance();
		return items.stream()
				.filter(msg -> CollectionUtils.isEmpty(msg.tags()) && msg.tags() != null)
				.flatMap(msg -> msg.tags().stream())
				.map(normalizer::normalize)
				.distinct()
				.toList();
	}

	private void savePostTags(List<RssFeedMessage> items) {
		TagNormalizerUtils normalizer = TagNormalizerUtils.getInstance();

		Map<String, List<String>> urlToTagsMap = items.stream()
				.filter(msg -> CollectionUtils.isEmpty(msg.tags()) && msg.tags() != null)
				.collect(Collectors.groupingBy(
					RssFeedMessage::url,
					Collectors.flatMapping(
						msg -> msg.tags().stream().map(normalizer::normalize),
						Collectors.toList()
					)
				))
				.entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					entry -> entry.getValue().stream().distinct().toList()
				));

		if (urlToTagsMap.isEmpty()) {
			return;
		}

		List<String> allUrls = new ArrayList<>();
		List<String> allTags = new ArrayList<>();

		urlToTagsMap.forEach((url, tags) ->
			tags.forEach(tag -> {
				allUrls.add(url);
				allTags.add(tag);
			})
		);

		customPostTagRepository.saveAllUrlAndTag(allUrls, allTags);
	}

	private List<Post> convertToPosts(List<RssFeedMessage> items) {
		return items.stream()
				.map(msg -> Post.create(
						snowflake.nextId(),
						msg.title(),
						msg.url(),
						msg.pubDate(),
						msg.description(),
						msg.thumbnail()
				))
				.toList();
	}
}
