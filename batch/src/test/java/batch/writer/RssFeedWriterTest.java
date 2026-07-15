package batch.writer;

import batch.constants.BatchConstants;
import batch.message.RssFeedMessage;
import batch.service.RssFeedDeadLetterPublisher;
import domain.repository.CustomBatchPostRepository;
import domain.repository.CustomBatchPostTagRepository;
import domain.repository.CustomBatchTagRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import org.springframework.batch.item.Chunk;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RssFeedWriterTest {

	@Mock
	private CustomBatchPostRepository customBatchPostRepository;

	@Mock
	private CustomBatchPostTagRepository customBatchPostTagRepository;

	@Mock
	private CustomBatchTagRepository customBatchTagRepository;

	@Mock
	private RssFeedDeadLetterPublisher deadLetterPublisher;

	@Mock
	private PlatformTransactionManager transactionManager;

	@Mock
	private TransactionStatus transactionStatus;

	private RssFeedWriter writer;

	@BeforeEach
	void setUp() {
		when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
		writer = new RssFeedWriter(
				customBatchPostRepository,
				customBatchPostTagRepository,
				customBatchTagRepository,
				deadLetterPublisher,
				transactionManager
		);
	}

	@Test
	void retriesTransientFailureAndDoesNotPublishToDlt() {
		RssFeedMessage message = message();
		doThrow(new IllegalStateException("temporary database failure"))
				.doNothing()
				.when(customBatchPostRepository)
				.saveAllPost(any());

		writer.write(new Chunk<>(List.of(message)));

		verify(customBatchPostRepository, times(2)).saveAllPost(any());
		verify(deadLetterPublisher, never()).publish(any(), any(), any(Integer.class));
	}

	@Test
	void publishesOriginalMessageToDltAfterThreeRetries() {
		RssFeedMessage message = message();
		doThrow(new IllegalStateException("permanent database failure"))
				.when(customBatchPostRepository)
				.saveAllPost(any());

		writer.write(new Chunk<>(List.of(message)));

		verify(customBatchPostRepository, times(4)).saveAllPost(any());
		verify(deadLetterPublisher).publish(eq(message), any(Throwable.class), eq(3));
	}

	@Test
	void countsOnlyUniqueUrlsAfterSuccessfulProcessing() {
		StepExecution stepExecution = new StepExecution("rss-step", new JobExecution(1L));
		writer.setStepExecution(stepExecution);

		writer.write(new Chunk<>(List.of(message(), message())));

		assertEquals(
				1L,
				stepExecution.getExecutionContext().getLong(BatchConstants.UNIQUE_POST_COUNT_KEY)
		);
	}

	private RssFeedMessage message() {
		return new RssFeedMessage(
				"title",
				"https://example.com/post",
				LocalDateTime.now(),
				Set.of(),
				"description",
				null,
				"example",
				"KO"
		);
	}
}
