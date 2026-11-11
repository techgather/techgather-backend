# 게시글 자동 분류

batch의 자동 분류와 API의 관리자 분류는 OpenAI Chat Completions API를 사용한다.
기본 모델은 `gpt-5.4-mini`, `reasoning-effort` 기본값은 `none`이다.
제목, 출처 사이트 이름, 태그와 선택 가능한 카테고리를 전달한다.

## 설정

API와 batch가 읽는 외부 설정 파일에 다음 값을 설정한다.
설정 파일은 저장소에 커밋하지 않는다.

```yaml
techgather:
  openai:
    api-key: ${OPENAI_API_KEY}
  classification:
    llm:
      enabled: true
      base-url: https://api.openai.com/v1
      model: gpt-5.4-mini
      reasoning-effort: none
```

새 키 이름은 `techgather.openai.api-key`이며, 이 값이 없으면
`OPENAI_API_KEY` 환경 변수를 사용한다. 키가 없거나 `enabled: false`이면
LLM 호출을 건너뛴다. 활성화 기본값은 `false`이다.

기존 NVIDIA 설정에서 전환할 때는 `techgather.nvidia.api-key` 대신 새 키를
등록한다. 외부 설정에서 `techgather.classification.llm.base-url`이나
`techgather.classification.llm.model`을 지정했다면 위 OpenAI 값으로 변경해야 한다.
설정 적용 후 API와 batch를 모두 재시작한다.

요청은 JSON 응답 형식을 지정하며, `max_completion_tokens: 512`를 사용한다.
다른 모델이나 추론 수준을 선택할 때는 해당 모델의 파라미터 지원과
추론 토큰을 포함한 출력 한도를 확인한다.

## 분류 실패 처리

- batch: 분류 불가 응답은 보류 상태로 변경한다. HTTP 오류 등 호출 자체가
  실패하면 상태를 유지하고 다음 실행에서 다시 시도한다.
- API: LLM 결과가 없으면 키워드 분류를 시도한다. HTTP 429 또는 503이면
  해당 실행의 추가 LLM 호출을 중단한다.

## 검증

```bash
./gradlew :batch:test --tests batch.service.OpenAiPostClassifierTest
./gradlew :api:test --tests api.service.OpenAiPostClassifierTest
```

테스트는 HTTP 요청과 응답을 모의하므로 실제 API 키나 유료 호출이 필요하지 않다.

공식 문서: [모델](https://developers.openai.com/api/docs/models/gpt-5.4-mini),
[Chat Completions](https://developers.openai.com/api/reference/resources/chat/subresources/completions/methods/create).
