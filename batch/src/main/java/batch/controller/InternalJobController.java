package batch.controller;

import batch.service.PostIngestJobService;
import batch.service.PostLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/internal/jobs")
@RequiredArgsConstructor
public class InternalJobController {

    private static final String INTERNAL_JOB_TOKEN_HEADER = "X-Internal-Job-Token";

    private final PostIngestJobService postIngestJobService;
    private final PostLookupService postLookupService;

    @Value("${internal.job-token:}")
    private String internalJobToken;

    @PostMapping("/post-ingest")
    public ResponseEntity<PostIngestJobService.PostIngestResult> postIngest(
            @RequestHeader(value = INTERNAL_JOB_TOKEN_HEADER, required = false) String requestToken
    ) throws Exception {
        assertAuthorized(requestToken);

        PostIngestJobService.PostIngestResult result = postIngestJobService.runPostIngest();
        if (!"COMPLETED".equals(result.jobStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/existing-post-urls")
    public ExistingPostUrlsResponse existingPostUrls(
            @RequestHeader(value = INTERNAL_JOB_TOKEN_HEADER, required = false) String requestToken,
            @RequestBody ExistingPostUrlsRequest request
    ) {
        assertAuthorized(requestToken);
        return new ExistingPostUrlsResponse(postLookupService.findExistingUrls(request.urls()));
    }

    private void assertAuthorized(String requestToken) {
        if (!StringUtils.hasText(internalJobToken)) {
            return;
        }
        if (!internalJobToken.equals(requestToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    public record ExistingPostUrlsRequest(List<String> urls) {
    }

    public record ExistingPostUrlsResponse(Set<String> existingUrls) {
    }
}
