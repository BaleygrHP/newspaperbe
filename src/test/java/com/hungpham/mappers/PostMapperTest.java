package com.hungpham.mappers;

import com.hungpham.dtos.PostDto;
import com.hungpham.entity.PostEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostMapperTest {

    private final PostMapper mapper = new PostMapper() {
        @Override
        public PostDto toDto(PostEntity entity) {
            return null;
        }

        @Override
        public PostEntity toEntity(PostDto dto) {
            return null;
        }

        @Override
        public void updateEntityFromDto(PostDto dto, PostEntity entity) {
            // no-op
        }
    };

    @Test
    void contentForDtoPrefersHtmlOverMarkdown() {
        PostEntity entity = new PostEntity();
        entity.setContentHtml("<p>HTML body</p>");
        entity.setContentMd("# Markdown body");

        String content = mapper.contentForDto(entity);

        assertEquals("<p>HTML body</p>", content);
    }

    @Test
    void contentForDtoRendersMarkdownWhenHtmlMissing() {
        PostEntity entity = new PostEntity();
        entity.setContentMd("~~done~~");

        String content = mapper.contentForDto(entity);

        assertEquals("<p><del>done</del></p>", content);
    }
}
