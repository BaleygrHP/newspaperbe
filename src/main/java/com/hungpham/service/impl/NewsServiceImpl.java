package com.hungpham.service.impl;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.dtos.NewUpTopDto;
import com.hungpham.dtos.NewsDto;
import com.hungpham.entity.CategoryEntity;
import com.hungpham.entity.NewUpTopEntity;
import com.hungpham.entity.NewsEntity;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.NewUpTopMapper;
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

    @Autowired
    private NewUpTopMapper newUpTopMapper;
    @Override
    public List<NewsDto> getAllNews() {
        List<NewsEntity>  newsData = newsRepository.findAll();
        List<NewsDto> newsDataDtos = new ArrayList<>();
        for (NewsEntity news: newsData) {
            UserEntity userEntity = getUserEntity(news.getAuthor());
            NewsDto newsDataDto = newsMapper.toDto(news);
            newsDataDto.setAuthor(userEntity.getNickName());
            newsDataDto.setCategoryName(news.getCategory().getCategoryName());
            newsDataDtos.add(newsDataDto);
        }
        return newsDataDtos;
    }

    @Override
    public NewsDto getNewsById(String id) {
        logger.info("Find new with id: {}", id);
        NewsEntity newsEntity =  newsRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("News not found " + id));
        return newsMapper.toDto(newsEntity);

    }

    @Override
    public List<NewsDto> getNewsByAuthor(String author) {
        logger.info("Find new with author: {}", author);
        List<NewsEntity> newsEntityList = newsRepository.getByAuthor(author);
        if (newsEntityList.isEmpty()){
            throw new BadRequestException("This author is not have any news");
        }
        List<NewsDto> newsDtoList = new ArrayList<>();
        for (NewsEntity newsEntity:newsEntityList){
            newsDtoList.add(newsMapper.toDto(newsEntity));
        }
        return newsDtoList;
    }

    @Override
    public NewsDto createNewNew(NewsDto newsDto) {
        UUID uuid = UUID.randomUUID();
        newsDto.setId(String.valueOf(uuid));
        newsDto.setCreatedDate(new Date().toString());
        logger.info("Create new with data: {}", newsDto);
        CategoryEntity categoryEntity = getCategoryEntity(newsDto.getCategoryName());
        NewsEntity newsEntity = newsMapper.toEntity(newsDto);
        newsEntity.setAuthor(newsDto.getAuthor());
        newsEntity.setCategory(categoryEntity);
        logger.info("Save news to db {}", newsEntity);
        return newsMapper.toDto(newsRepository.save(newsEntity));
    }

    @Override
    public NewsDto updateNew(NewsDto newsDto) {
        logger.info("Update new with data: {}", newsDto);
        if (null == newsDto.getId()) {
            throw new BadRequestException("Not found new id to update");
        }
        NewsEntity newsEntity = newsRepository.findById(newsDto.getId()).orElseThrow(
                ()-> new EntityNotFoundException("News not found for update" + newsDto.getId()));
        newsDto.setCreatedDate(newsEntity.getCreatedDate());
        newsDto.setUpdatedDate(new Date().toString());
        CategoryEntity categoryEntity = getCategoryEntity(newsDto.getCategoryName());
        NewsEntity finalNewEntity = newsMapper.toEntity(newsDto);
        finalNewEntity.setAuthor(newsDto.getAuthor());
        finalNewEntity.setCategory(categoryEntity);
        return newsMapper.toDto(newsRepository.save(finalNewEntity));
    }

    @Override
    public NewsDto deleteNew(String id) {
        logger.info("Delete new with id: {}", id);
        NewsEntity newsEntity = newsRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("News not found for delete" + id));
        if (newsEntity.getDeleteFlag()) {
            return newsMapper.toDto(newsEntity);
        }
        newsEntity.setDeleteFlag(true);
        newsEntity.setUpdatedDate(new Date().toString());
        return newsMapper.toDto(newsRepository.save(newsEntity));
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
    public NewUpTopDto getUpTopNew() {
        logger.info("Get top new");
        NewUpTopEntity newUpTopEntity = newUpTopRepository.findAll().get(0);
        UserEntity userEntity = getUserEntity(newUpTopEntity.getAuthor());
        NewUpTopDto newUpTopDto = newUpTopMapper.toDto(newUpTopEntity);
        newUpTopDto.setAuthor(userEntity.getNickName());
        newUpTopDto.setCategoryName(newUpTopEntity.getCategory().getCategoryName());
        return newUpTopDto;
    }

    @Override
    public Map<String, NewsDto> getBodyNew() {
        logger.info("Get body new");
        Map<String, NewsDto> dataMap = new HashMap<>();
        List<Object[]> dataBodyNews = newsRepository.getDataBodyNews();
        for (Object[] data: dataBodyNews) {
            NewsDto newsBody = new NewsDto();
            newsBody.setId(data[0].toString());
            newsBody.setTitle(data[1].toString());
            newsBody.setImageNew(data[2] != null ? data[2].toString() : "");
            newsBody.setShortDescription(data[3].toString());
            newsBody.setAuthor(data[4].toString());
            newsBody.setCreatedDate(data[5].toString());
            newsBody.setUpdatedDate(data[6].toString());
            newsBody.setCategoryName(data[8].toString());

        }
        dataMap.put(data[7].toString(), newsBody);
        return dataMap;
    }
}
