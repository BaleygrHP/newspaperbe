package com.hungpham.service;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class MarkdownRenderService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRenderService() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                TaskListExtension.create(),
                StrikethroughExtension.create(),
                AutolinkExtension.create()
        ));
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public String renderMarkdownToHtml(String markdown) {
        if (isBlank(markdown)) return "";
        com.vladsch.flexmark.util.ast.Node markdownDocument = parser.parse(markdown);
        String html = renderer.render(markdownDocument);
        return injectMathMarkers(html);
    }

    public String extractPlainText(String html) {
        if (isBlank(html)) return "";
        return Jsoup.parse(html).text().trim();
    }

    String injectMathMarkers(String html) {
        if (isBlank(html)) return "";

        Document document = Jsoup.parseBodyFragment(html);
        replaceMathInElement(document.body());

        return document.body().html();
    }

    private void replaceMathInElement(Element element) {
        List<Node> children = new ArrayList<Node>(element.childNodes());
        for (Node child : children) {
            if (child instanceof TextNode) {
                replaceMathInTextNode((TextNode) child);
                continue;
            }
            if (!(child instanceof Element)) continue;
            Element childElement = (Element) child;
            if (isSkippedTag(childElement.tagName())) continue;
            replaceMathInElement(childElement);
        }
    }

    private void replaceMathInTextNode(TextNode textNode) {
        String raw = textNode.getWholeText();
        if (isBlank(raw) || raw.indexOf('$') < 0) return;

        List<MathSegment> segments = parseMathSegments(raw);
        if (segments.isEmpty()) return;
        if (segments.size() == 1 && segments.get(0).type == SegmentType.TEXT) return;

        org.jsoup.nodes.Node cursor = textNode;
        for (MathSegment segment : segments) {
            if (segment.value.isEmpty()) continue;
            if (segment.type == SegmentType.TEXT) {
                cursor.before(new TextNode(segment.value));
                continue;
            }
            if (segment.type == SegmentType.BLOCK_MATH) {
                Element block = new Element("span");
                block.attr("data-math-block", segment.value);
                cursor.before(block);
                continue;
            }
            Element inline = new Element("span");
            inline.attr("data-math-inline", segment.value);
            cursor.before(inline);
        }
        cursor.remove();
    }

    private List<MathSegment> parseMathSegments(String input) {
        List<MathSegment> segments = new ArrayList<MathSegment>();
        StringBuilder plain = new StringBuilder();
        int i = 0;

        while (i < input.length()) {
            char ch = input.charAt(i);
            if (ch != '$' || isEscaped(input, i)) {
                plain.append(ch);
                i++;
                continue;
            }

            boolean block = (i + 1 < input.length()) && input.charAt(i + 1) == '$' && !isEscaped(input, i + 1);
            int closeIndex = findClosingDelimiter(input, i + (block ? 2 : 1), block);
            if (closeIndex < 0) {
                plain.append(ch);
                i++;
                continue;
            }

            String formula = input.substring(i + (block ? 2 : 1), closeIndex).trim();
            if (formula.isEmpty()) {
                plain.append(input, i, closeIndex + (block ? 2 : 1));
                i = closeIndex + (block ? 2 : 1);
                continue;
            }

            appendPlain(segments, plain);
            segments.add(new MathSegment(block ? SegmentType.BLOCK_MATH : SegmentType.INLINE_MATH, formula));
            i = closeIndex + (block ? 2 : 1);
        }

        appendPlain(segments, plain);
        return segments;
    }

    private int findClosingDelimiter(String input, int fromIndex, boolean block) {
        if (block) {
            for (int i = fromIndex; i + 1 < input.length(); i++) {
                if (input.charAt(i) == '$'
                        && input.charAt(i + 1) == '$'
                        && !isEscaped(input, i)) {
                    return i;
                }
            }
            return -1;
        }

        for (int i = fromIndex; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '\n' || ch == '\r') {
                return -1;
            }
            if (ch != '$') continue;
            if (isEscaped(input, i)) continue;
            if (i - 1 >= 0 && input.charAt(i - 1) == '$') continue;
            if (i + 1 < input.length() && input.charAt(i + 1) == '$') continue;
            return i;
        }
        return -1;
    }

    private boolean isSkippedTag(String tagName) {
        String tag = tagName == null ? "" : tagName.toLowerCase(Locale.ROOT);
        return "pre".equals(tag) || "code".equals(tag) || "script".equals(tag) || "style".equals(tag);
    }

    private boolean isEscaped(String input, int index) {
        int slashCount = 0;
        for (int i = index - 1; i >= 0 && input.charAt(i) == '\\'; i--) {
            slashCount++;
        }
        return slashCount % 2 == 1;
    }

    private void appendPlain(List<MathSegment> segments, StringBuilder plain) {
        if (plain.length() == 0) return;
        segments.add(new MathSegment(SegmentType.TEXT, plain.toString()));
        plain.setLength(0);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private enum SegmentType {
        TEXT,
        INLINE_MATH,
        BLOCK_MATH
    }

    private static class MathSegment {
        private final SegmentType type;
        private final String value;

        private MathSegment(SegmentType type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}
