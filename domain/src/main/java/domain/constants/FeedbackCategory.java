package domain.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackCategory {
    BUG("오류·장애 신고"),
    CONTENT("콘텐츠 관련 의견"),
    FEATURE("신규 기능 제안"),
    UX("사용성·화면 개선"),
    ETC("기타");

    private final String description;
}
