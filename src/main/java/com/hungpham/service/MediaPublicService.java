package com.hungpham.service;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.dtos.PublicMediaDto;
import org.springframework.data.domain.Page;

public interface MediaPublicService {
    Page<PublicMediaDto> getGallery(MediaKindEnum kind, int page, int size);
    PublicMediaDto getActiveById(String id);
}
