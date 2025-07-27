package com.hungpham.service;

import com.hungpham.dtos.NewUpTopDto;
import com.hungpham.dtos.NewsDto;
import com.hungpham.entity.NewUpTopEntity;
import com.hungpham.entity.NewsEntity;

import java.util.List;
import java.util.Map;

public interface NewsService {
    List<NewsDto> getAllNews();
    NewsDto getNewsById(String id);
    List<NewsDto> getNewsByAuthor(String author);
    NewsDto createNewNew(NewsDto newsDto);
    NewsDto updateNew(NewsDto newsDto);
    NewsDto deleteNew(String id);
    NewUpTopDto getUpTopNew();
    Map<String, NewsDto> getBodyNew();
}
