package com.hungpham.controller.publicapi;

import com.hungpham.dtos.SectionDto;
import com.hungpham.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/sections")
public class SectionPublicController {

    @Autowired
    private SectionService sectionService;

    @GetMapping("/menu")
    public List<SectionDto> getMenu() {
        return sectionService.getPublicMenu();
    }

    @GetMapping("/{key}")
    public SectionDto getByKey(@PathVariable String key) {
        return sectionService.getByKey(key);
    }
}
