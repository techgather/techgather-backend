package domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {

    KO("KO", "국내"),
    EN("EN", "국외");

    private final String code;
    private final String description;

    public static Language fromCode(String code) {
        for (Language language : Language.values()) {
            if (language.getCode().equals(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unknown language code: " + code);
    }
}
