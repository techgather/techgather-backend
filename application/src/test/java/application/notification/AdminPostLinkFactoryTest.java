package application.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminPostLinkFactoryTest {

    @Test
    void replacesPostIdPlaceholder() {
        AdminPostLinkFactory factory =
                new AdminPostLinkFactory("https://admin.example.com/posts/{postId}");

        assertThat(factory.create(123L))
                .isEqualTo("https://admin.example.com/posts/123");
        assertThat(factory.markdownLink("제목 [테스트]", 123L))
                .isEqualTo("[제목 \\[테스트\\]](https://admin.example.com/posts/123)");
    }

    @Test
    void fallsBackToTitleAndIdWhenTemplateIsMissing() {
        AdminPostLinkFactory factory = new AdminPostLinkFactory("");

        assertThat(factory.markdownLink("게시글", 123L))
                .isEqualTo("게시글 (`123`)");
    }
}
