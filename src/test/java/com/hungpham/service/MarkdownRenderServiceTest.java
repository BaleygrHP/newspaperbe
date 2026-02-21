package com.hungpham.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownRenderServiceTest {

    private final MarkdownRenderService service = new MarkdownRenderService();

    @Test
    void rendersGfmTable() {
        String markdown = "| Name | Score |\n| --- | --- |\n| Alice | 10 |";

        String html = service.renderMarkdownToHtml(markdown);

        assertTrue(html.contains("<table"));
        assertTrue(html.contains("<thead>"));
        assertTrue(html.contains("<tbody>"));
    }

    @Test
    void rendersTaskListCheckboxes() {
        String markdown = "- [ ] todo\n- [x] done";

        String html = service.renderMarkdownToHtml(markdown);

        assertTrue(html.contains("type=\"checkbox\""));
        assertTrue(html.contains("checked"));
    }

    @Test
    void rendersStrikethrough() {
        String markdown = "Before ~~remove me~~ after";

        String html = service.renderMarkdownToHtml(markdown);

        assertTrue(html.contains("<del>remove me</del>"));
    }

    @Test
    void rendersAutolink() {
        String markdown = "Visit https://example.com for docs.";

        String html = service.renderMarkdownToHtml(markdown);

        assertTrue(
                html.contains("href=\"https://example.com\"")
                        || html.contains("href=\"https://example.com/\""));
    }

    @Test
    void rendersGfmAndMathTogether() {
        String markdown = "| A |\n| --- |\n| 1 |\n\nInline: $a+b$.\n\n$$\nc=d\n$$";

        String html = service.renderMarkdownToHtml(markdown);

        assertTrue(html.contains("<table"));
        assertTrue(html.contains("data-math-inline=\"a+b\""));
        assertTrue(html.contains("data-math-block=\"c=d\""));
    }

    @Test
    void doesNotParseMathInsideCodeBlock() {
        String markdown = "```java\nString s = \"$x$\";\n```";

        String html = service.renderMarkdownToHtml(markdown);

        assertTrue(html.contains("<code"));
        assertTrue(html.contains("$x$"));
        assertFalse(html.contains("data-math-inline=\"x\""));
    }
}
