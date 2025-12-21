package com.hungpham.mappers;

import com.hungpham.common.enums.SectionEnum;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.entity.SectionEntity;
import com.hungpham.entity.UserEntity;
import com.hungpham.repository.SectionRepository;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SectionEnumMapper {

    @Autowired
    private SectionRepository sectionRepository;

    /**
     * Entity.section (SectionEntity) -> Dto.section (SectionEnum)
     */


    @Named("fromSectionEnum")
    public SectionEnum fromSectionEnum(SectionEntity sectionEntity) {
        if (sectionEntity == null) return null;
        // sections.key trong DB (varchar64) thường map sang enum
        String key = sectionEntity.getKey();
        if (key == null || key.trim().isEmpty()) return null;

        // Bạn đã có SectionEnum.from(String) (trước bạn nói rồi)
        return SectionEnum.from(key.trim());
    }

    /**
     * Dto.section (SectionEnum) -> Entity.section (SectionEntity)
     * => cần lookup SectionEntity theo key
     */
    @Named("toSectionEnum")
    public SectionEntity toSectionEnum(SectionEnum sectionEnum) {
        if (sectionEnum == null) return null;

        // Giả định enum name tương ứng key (thường key lưu lowercase)
        String key = sectionEnum.name().toLowerCase();

        return sectionRepository.findByKey(key)
                .orElseThrow(() -> new EntityNotFoundException("Section not found by key: " + key));
    }
}
