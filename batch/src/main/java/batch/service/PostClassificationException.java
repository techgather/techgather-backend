// LLM 분류 호출 자체가 실패했음을 알리는 예외 — 보류가 아니라 다음 실행에서 재시도할 대상
package batch.service;

public class PostClassificationException extends RuntimeException {

    public PostClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
