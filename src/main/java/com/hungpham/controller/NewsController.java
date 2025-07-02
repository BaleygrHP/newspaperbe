package com.hungpham.controller;

import com.hungpham.dtos.NewsDto;
import com.hungpham.entity.NewUpTopEntity;
import com.hungpham.entity.NewsEntity;
import com.hungpham.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("news")
// remove gấp sau khi config auth2.0
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend origin
public class NewsController {
    @Autowired
    private NewsService newsService;

    @GetMapping("/get-all-news")
    public List<NewsDto> getAllNews() {
        return newsService.getAllNews();
    }

    @GetMapping("/get-by-id")
    public NewsEntity getNewsById(@RequestParam String id) {
        return newsService.getNewsById(id);
    }

    @GetMapping("/get-all-by-author")
    public List<NewsEntity> getNewsByAuthor(@RequestParam String author) {
        return newsService.getNewsByAuthor(author);
    }

    @PostMapping("/create-new-news")
    public NewsEntity createNewNews(@RequestBody NewsDto newsDto){
        return newsService.createNewNew(newsDto);
    }

    @PutMapping("/update-news")
    public NewsEntity updateNews(@RequestBody NewsDto newsDto){
        return newsService.updateNew(newsDto);
    }

    @DeleteMapping
    public NewsEntity deleteNew(@RequestParam String id){
       return newsService.deleteNew(id);
    }

    @GetMapping("/get-up-top-new")
    public NewUpTopEntity getUpTopNew() {
        return newsService.getUpTopNew();
    }

    @GetMapping("/get-body-new")
    public Map<String, NewsEntity> getBodyNew() {
        return newsService.getBodyNew();
    }
}
