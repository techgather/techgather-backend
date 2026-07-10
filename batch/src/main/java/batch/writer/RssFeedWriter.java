package batch.writer;

import application.generator.SnowFlake;
import batch.message.RssFeedMessage;
import domain.entity.Post;
import domain.entity.Tag;
import domain.repository.CustomBatchPostRepository;
import domain.repository.CustomBatchPostTagRepository;
import domain.repository.CustomBatchTagRepository;
import domain.util.TagNormalizerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import batch.service.RssFeedDeadLetterPublisher;

import java.util.*;
import java.util.stream.Collectors;

@Component
@StepScope
@Slf4j
public class RssFeedWriter implements ItemWriter<RssFeedMessage> {

	private static final int MAX_RETRIES = 3;
	private static final long RETRY_BACKOFF_MILLIS = 100L;

	private final CustomBatchPostRepository customBatchPostRepository;
	private final CustomBatchPostTagRepository customBatchPostTagRepository;
	private final CustomBatchTagRepository customBatchTagRepository;
	private final RssFeedDeadLetterPublisher deadLetterPublisher;
	private final TransactionTemplate itemTransactionTemplate;
	private final SnowFlake snowflake = SnowFlake.getInstance();

	public RssFeedWriter(
			CustomBatchPostRepository customBatchPostRepository,
			CustomBatchPostTagRepository customBatchPostTagRepository,
			CustomBatchTagRepository customBatchTagRepository,
			RssFeedDeadLetterPublisher deadLetterPublisher,
			PlatformTransactionManager transactionManager
	) {
		this.customBatchPostRepository = customBatchPostRepository;
		this.customBatchPostTagRepository = customBatchPostTagRepository;
		this.customBatchTagRepository = customBatchTagRepository;
		this.deadLetterPublisher = deadLetterPublisher;
		this.itemTransactionTemplate = new TransactionTemplate(transactionManager);
		this.itemTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

    @Override
    public void write(Chunk<? extends RssFeedMessage> chunk) {
        if (chunk.isEmpty()) {
            return;
        }

		chunk.forEach(this::processWithRetry);
	}

	private void processWithRetry(RssFeedMessage item) {
		RuntimeException lastFailure = null;

		for (int retryCount = 0; retryCount <= MAX_RETRIES; retryCount++) {
			try {
				itemTransactionTemplate.executeWithoutResult(status -> processTagsAndPosts(List.of(item)));
				return;
			} catch (RuntimeException e) {
				lastFailure = e;
				if (retryCount == MAX_RETRIES) {
					break;
				}

				log.warn(
						"Post ingest failed. retry={}/{}, url={}",
						retryCount + 1,
						MAX_RETRIES,
						item.url(),
						e
				);
				sleepBeforeRetry(retryCount + 1);
			}
		}

		deadLetterPublisher.publish(item, lastFailure, MAX_RETRIES);
	}

	private void sleepBeforeRetry(int retryCount) {
		try {
			Thread.sleep(RETRY_BACKOFF_MILLIS * retryCount);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while retrying post ingest", e);
		}
	}

    private void processTagsAndPosts(List<RssFeedMessage> items) {
        List<String> tagNames = extractTagNames(items);
        if (!CollectionUtils.isEmpty(tagNames)) {
            List<Tag> tags = tagNames.stream()
                    .map(name -> Tag.create(snowflake.nextId(), name))
                    .toList();
            customBatchTagRepository.saveAllTag(tags);
        }

        List<Post> posts = convertToPosts(items);
        if (!CollectionUtils.isEmpty(posts)) {
            customBatchPostRepository.saveAllPost(posts);
        }
        savePostTags(items);
    }

    private List<String> extractTagNames(List<RssFeedMessage> items) {
        TagNormalizerUtils normalizer = TagNormalizerUtils.getInstance();
        return items.stream()
                .filter(msg -> !CollectionUtils.isEmpty(msg.tags()))
                .flatMap(msg -> msg.tags().stream())
                .map(normalizer::normalize)
                .distinct()
                .sorted()
                .toList();
    }

    private void savePostTags(List<RssFeedMessage> items) {
        TagNormalizerUtils normalizer = TagNormalizerUtils.getInstance();

        Map<String, List<String>> urlToTagsMap = items.stream()
                .filter(msg -> !CollectionUtils.isEmpty(msg.tags()))
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

        customBatchPostTagRepository.saveAllUrlAndTag(allUrls, allTags);
    }

    private List<Post> convertToPosts(List<RssFeedMessage> items) {
        return items.stream()
                .map(msg -> Post.create(
                        snowflake.nextId(),
                        msg.title(),
                        msg.url(),
                        msg.pubDate(),
                        msg.thumbnail(),
                        msg.sourceSiteName(),
                        msg.language()
                ))
                .toList();
    }
}
