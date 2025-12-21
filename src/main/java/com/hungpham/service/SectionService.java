package com.hungpham.service;

import com.hungpham.dtos.SectionDto;
import com.hungpham.requests.section.CreateSectionRequest;
import com.hungpham.requests.section.UpdateSectionRequest;

import java.util.List;

public interface SectionService {
    List<SectionDto> getPublicMenu();                 // active + visibility=PUBLIC
    List<SectionDto> getAllActive();                  // admin helper
    SectionDto getByKey(String key);                  // public routing

    // admin
    List<SectionDto> adminListAll(String q);
    SectionDto adminGetById(String id);
    SectionDto adminCreate(CreateSectionRequest req, String actorUserId);
    SectionDto adminUpdate(String id, UpdateSectionRequest req, String actorUserId);
    SectionDto adminToggleActive(String id, String actorUserId);

}
