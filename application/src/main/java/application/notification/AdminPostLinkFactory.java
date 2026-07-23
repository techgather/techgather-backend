package application.notification;

import org.springframework.util.StringUtils;

public class AdminPostLinkFactory {

    private static final String POST_ID_PLACEHOLDER = "{postId}";

    private final String urlTemplate;

    public AdminPostLinkFactory(String urlTemplate) {
        this.urlTemplate = urlTemplate == null ? "" : urlTemplate.trim();
    }

    public String create(long postId) {
        if (!StringUtils.hasText(urlTemplate)) {
            return "";
        }
        if (urlTemplate.contains(POST_ID_PLACEHOLDER)) {
            return urlTemplate.replace(POST_ID_PLACEHOLDER, Long.toString(postId));
        }
        return urlTemplate + postId;
    }

    public String markdownLink(String label, long postId) {
        String safeLabel = label == null || label.isBlank()
                ? Long.toString(postId)
                : label.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
        String url = create(postId);
        if (!StringUtils.hasText(url)) {
            return safeLabel + " (`" + postId + "`)";
        }
        return "[" + safeLabel + "](" + url + ")";
    }
}
