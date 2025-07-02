package com.hungpham.service;

import com.hungpham.dtos.NewsDto;
import com.hungpham.entity.NewUpTopEntity;
import com.hungpham.entity.NewsEntity;

import java.util.List;
import java.util.Map;

public interface NewsService {
    List<NewsDto> getAllNews();
    NewsEntity getNewsById(String id);
    List<NewsEntity> getNewsByAuthor(String author);
    NewsEntity createNewNew(NewsDto newsDto);
    NewsEntity updateNew(NewsDto newsDto);
    NewsEntity deleteNew(String id);
    NewUpTopEntity getUpTopNew();
    Map<String, NewsEntity> getBodyNew();
}
