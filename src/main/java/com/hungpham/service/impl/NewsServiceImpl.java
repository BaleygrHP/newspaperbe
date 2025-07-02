package com.hungpham.service.impl;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.dtos.NewsDto;
import com.hungpham.entity.CategoryEntity;
import com.hungpham.entity.NewUpTopEntity;
import com.hungpham.entity.NewsEntity;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.NewsMapper;
import com.hungpham.repository.CategoriesRepository;
import com.hungpham.repository.NewUpTopRepository;
import com.hungpham.repository.NewsRepository;
import com.hungpham.repository.UserRepository;
import com.hungpham.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class NewsServiceImpl implements NewsService {
    private static final Logger logger = LoggerFactory.getLogger(NewsServiceImpl.class);
    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private NewUpTopRepository newUpTopRepository;

    @Autowired
    private NewsMapper newsMapper;
    @Override
    public List<NewsDto> getAllNews() {
        List<NewsEntity>  newsData = newsRepository.findAll();
        List<NewsDto> newsDataDtos = new ArrayList<>();
        NewsDto newsDataDto = new NewsDto();
        for (NewsEntity news: newsData) {
            UserEntity userEntity = getUserEntity(news.getAuthor());
            newsDataDto = newsMapper.toDto(news);
            newsDataDto.setAuthor(userEntity.getNickName());
            newsDataDto.setCategoryName(news.getCategory().getCategoryName());
            newsDataDtos.add(newsDataDto);
        }
        return newsDataDtos;
    }

    @Override
    public NewsEntity getNewsById(String id) {
        logger.info("Find new with id: {}", id);
        return newsRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("News not found " + id));
    }

    @Override
    public List<NewsEntity> getNewsByAuthor(String author) {
        logger.info("Find new with author: {}", author);
        List<NewsEntity> newsEntityList = newsRepository.getByAuthor(author);
        if (newsEntityList.isEmpty()){
            throw new BadRequestException("This author is not have any news");
        }
        return newsEntityList;
    }

    @Override
    public NewsEntity createNewNew(NewsDto newsDto) {
        UUID uuid = UUID.randomUUID();
        newsDto.setId(String.valueOf(uuid));
        newsDto.setCreatedDate(new Date().toString());
        logger.info("Create new with data: {}", newsDto);
//        UserEntity userEntity = getUserEntity(newsDto.getAuthor());
        CategoryEntity categoryEntity = getCategoryEntity(newsDto.getCategoryName());
        NewsEntity newsEntity = newsMapper.toEntity(newsDto);
        newsEntity.setAuthor(newsDto.getAuthor());
        newsEntity.setCategory(categoryEntity);
        logger.info("Save news to db {}", newsEntity);
        return newsRepository.save(newsEntity);
    }

    @Override
    public NewsEntity updateNew(NewsDto newsDto) {
        logger.info("Update new with data: {}", newsDto);
        if (null == newsDto.getId()) {
            throw new BadRequestException("Not found new id to update");
        }
        NewsEntity newsEntity = newsRepository.findById(newsDto.getId()).orElseThrow(
                ()-> new EntityNotFoundException("News not found for update" + newsDto.getId()));
        newsDto.setCreatedDate(newsEntity.getCreatedDate().toString());
        newsDto.setUpdatedDate(new Date().toString());
//        UserEntity userEntity = getUserEntity(newsDto.getAuthor());
        CategoryEntity categoryEntity = getCategoryEntity(newsDto.getCategoryName());
        NewsEntity finalNewEntity = newsMapper.toEntity(newsDto);
        finalNewEntity.setAuthor(newsDto.getAuthor());
        finalNewEntity.setCategory(categoryEntity);
        return newsRepository.save(finalNewEntity);
    }

    @Override
    public NewsEntity deleteNew(String id) {
        logger.info("Delete new with id: {}", id);
        NewsEntity newsEntity = newsRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("News not found for delete" + id));
        if (newsEntity.getDeleteFlag()) {
            return newsEntity;
        }
        newsEntity.setDeleteFlag(true);
        newsEntity.setUpdatedDate(new Date().toString());
        return newsRepository.save(newsEntity);
    }

    private CategoryEntity getCategoryEntity(String categoryName) {
        CategoryEntity categoryEntity = categoriesRepository.findByCategoryName(categoryName).orElseThrow(()
                -> new EntityNotFoundException("Category not found " + categoryName));
        logger.info("The Category data is {}", categoryEntity);
        return categoryEntity;
    }

    private UserEntity getUserEntity(String author) {
        UserEntity userEntity = userRepository.findById(author).orElseThrow(()
                -> new EntityNotFoundException("User not found " + author));
        logger.info("The Author data is {}", userEntity);
        return userEntity;
    }

    @Override
    public NewUpTopEntity getUpTopNew() {
        logger.info("Get top new");
        return newUpTopRepository.findAll().get(0);
    }

    @Override
    public Map<String, NewsEntity> getBodyNew() {
        logger.info("Get body new");
        Map<String, NewsEntity> dataMap = new HashMap<>();
        List<Object[]> dataBodyNews = newsRepository.getDataBodyNews();
        for (Object[] data: dataBodyNews) {
            NewsEntity newsBody = new NewsEntity();
            newsBody.setId(data[0].toString());
            newsBody.setTitle(data[1].toString());
            newsBody.setImageNew(data[2].toString());
            newsBody.setShortDescription(data[3].toString());
            newsBody.setAuthor(data[4].toString());
            newsBody.setCreatedDate(data[5].toString());
            newsBody.setUpdatedDate(data[6].toString());
            dataMap.put(data[7].toString(), newsBody);
        }
        return dataMap;
    }
}
