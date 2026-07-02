package batch.controller;

import batch.service.PostIngestJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/jobs")
@RequiredArgsConstructor
public class InternalJobController {

    private static final String INTERNAL_JOB_TOKEN_HEADER = "X-Internal-Job-Token";

    private final PostIngestJobService postIngestJobService;

    @Value("${internal.job-token:}")
    private String internalJobToken;

    @PostMapping("/post-ingest")
    public ResponseEntity<PostIngestJobService.PostIngestResult> postIngest(
            @RequestHeader(value = INTERNAL_JOB_TOKEN_HEADER, required = false) String requestToken
    ) throws Exception {
        assertAuthorized(requestToken);

        PostIngestJobService.PostIngestResult result = postIngestJobService.runPostIngestAndClassify();
        if (!"COMPLETED".equals(result.jobStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    private void assertAuthorized(String requestToken) {
        if (!StringUtils.hasText(internalJobToken)) {
            return;
        }
        if (!internalJobToken.equals(requestToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
