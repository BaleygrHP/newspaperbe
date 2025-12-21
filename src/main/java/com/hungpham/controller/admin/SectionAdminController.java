package com.hungpham.controller.admin;

import com.hungpham.dtos.SectionDto;
import com.hungpham.requests.section.CreateSectionRequest;
import com.hungpham.requests.section.UpdateSectionRequest;
import com.hungpham.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sections")
public class SectionAdminController {

    @Autowired
    private SectionService sectionService;

    private String actor(@RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return actorUserId;
    }

    @GetMapping
    public List<SectionDto> list(@RequestParam(value = "q", required = false) String q) {
        return sectionService.adminListAll(q);
    }

    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable String id) {
        return sectionService.adminGetById(id);
    }

    @PostMapping
    public SectionDto create(@RequestBody CreateSectionRequest req,
                             @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return sectionService.adminCreate(req, actor(actorUserId));
    }

    @PutMapping("/{id}")
    public SectionDto update(@PathVariable String id,
                             @RequestBody UpdateSectionRequest req,
                             @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return sectionService.adminUpdate(id, req, actor(actorUserId));
    }

    @PatchMapping("/{id}/toggle")
    public SectionDto toggle(@PathVariable String id,
                             @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return sectionService.adminToggleActive(id, actor(actorUserId));
    }
}
