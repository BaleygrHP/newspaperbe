package com.hungpham.controller.admin;

import com.hungpham.config.security.AuthContext;
import com.hungpham.dtos.SectionDto;
import com.hungpham.requests.section.CreateSectionRequest;
import com.hungpham.requests.section.UpdateSectionRequest;
import com.hungpham.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sections")
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class SectionAdminController {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private AuthContext authContext;

    @GetMapping
    public List<SectionDto> list(@RequestParam(value = "q", required = false) String q) {
        return sectionService.adminListAll(q);
    }

    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable String id) {
        return sectionService.adminGetById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public SectionDto create(@RequestBody CreateSectionRequest req) {
        return sectionService.adminCreate(req, authContext.requireUserId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public SectionDto update(@PathVariable String id, @RequestBody UpdateSectionRequest req) {
        return sectionService.adminUpdate(id, req, authContext.requireUserId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle")
    public SectionDto toggle(@PathVariable String id) {
        return sectionService.adminToggleActive(id, authContext.requireUserId());
    }
}
