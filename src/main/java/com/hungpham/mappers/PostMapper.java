package com.hungpham.mappers;
import com.hungpham.dtos.PostDto;
import com.hungpham.entity.PostEntity;
import com.hungpham.service.MarkdownRenderService;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class, SectionEnumMapper.class})
public interface PostMapper {
    MarkdownRenderService MARKDOWN_RENDER_SERVICE = new MarkdownRenderService();

    // Entity -> DTO
    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toUuid"),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "ldtToString"),
            @Mapping(target = "content", source = "entity", qualifiedByName = "contentForDto"),
            // Entity.section (enum) -> Dto.section (string)
            @Mapping(target = "section", source = "section", qualifiedByName = "fromSectionEnum")
    })
    PostDto toDto(PostEntity entity);

    // DTO -> Entity
    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toBytes"),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "stringToLdt"),
            // Dto.section (string) -> Entity.section (enum)
            @Mapping(target = "section", source = "section", qualifiedByName = "toSectionEnum")
    })
    PostEntity toEntity(PostDto dto);

    // Update existing entity from dto (ignore null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "stringToLdt"),
            @Mapping(target = "section", source = "section", qualifiedByName = "toSectionEnum")
    })
    void updateEntityFromDto(PostDto dto, @MappingTarget PostEntity entity);

    @Named("contentForDto")
    default String contentForDto(PostEntity entity) {
        if (entity == null) return null;
        String html = entity.getContentHtml();
        if (html != null && !html.isEmpty()) return html;
        String md = entity.getContentMd();
        if (md != null && !md.isEmpty()) return MARKDOWN_RENDER_SERVICE.renderMarkdownToHtml(md);
        return entity.getContentJson();
    }
}
