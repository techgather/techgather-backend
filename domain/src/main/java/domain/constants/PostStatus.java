package domain.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {

    DISCARDED("DISCARDED", "검수 과정에서 가치가 없다고 판단되는 경우 가지는 상태값"),
    NOT_PUBLISHED("NOT_PUBLISHED", "배치를 거쳐 수집된 포스트가 가지는 초기 상태값"),
    PUBLISHED("PUBLISHED", "검수를 거쳐 발행해도 된다고 판단되는 경우 가지는 상태값"),
    ON_HOLD("ON_HOLD", "검수 과정에서 의견이 달라 보류될 경우 가지는 상태값");

    private final String code;
    private final String description;
}
