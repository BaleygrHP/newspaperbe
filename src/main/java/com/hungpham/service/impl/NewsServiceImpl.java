package com.hungpham.service.impl;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.dtos.NewsDto;
import com.hungpham.entity.CategoryEntity;
import com.hungpham.entity.NewsEntity;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.NewsMapper;
import com.hungpham.repository.CategoriesRepository;
import com.hungpham.repository.NewsRepository;
import com.hungpham.repository.UserRepository;
import com.hungpham.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    private NewsMapper newsMapper;
    @Override
    public List<NewsEntity> getAllNews() {
        return newsRepository.findAll();
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
        newsDto.setCreatedDate(new Date());
        logger.info("Create new with data: {}", newsDto);
        UserEntity userEntity = getUserEntity(newsDto);
        CategoryEntity categoryEntity = getCategoryEntity(newsDto);
        NewsEntity newsEntity = newsMapper.toEntity(newsDto);
        newsEntity.setAuthor(userEntity);
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
        newsDto.setCreatedDate(newsEntity.getCreatedDate());
        newsDto.setUpdatedDate(new Date());
        UserEntity userEntity = getUserEntity(newsDto);
        CategoryEntity categoryEntity = getCategoryEntity(newsDto);
        NewsEntity finalNewEntity = newsMapper.toEntity(newsDto);
        finalNewEntity.setAuthor(userEntity);
        finalNewEntity.setCategory(categoryEntity);
        return newsRepository.save(finalNewEntity);
    }

    @Override
    public NewsEntity deleteNew(String id) {
        logger.info("Delete new with id: {}", id);
        NewsEntity newsEntity = newsRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("News not found for delete" + id));
        newsEntity.setDeleteFlag(true);
        newsEntity.setUpdatedDate(new Date());
        return newsRepository.save(newsEntity);
    }
    private CategoryEntity getCategoryEntity(NewsDto newsDto) {
        CategoryEntity categoryEntity = categoriesRepository.findByCategoryName(newsDto.getCategoryName()).orElseThrow(()
                -> new EntityNotFoundException("Category not found " + newsDto.getCategoryName()));
        logger.info("The Category data is {}", categoryEntity);
        return categoryEntity;
    }

    private UserEntity getUserEntity(NewsDto newsDto) {
        UserEntity userEntity = userRepository.findByUserName(newsDto.getAuthor()).orElseThrow(()
                -> new EntityNotFoundException("User not found " + newsDto.getAuthor()));
        logger.info("The Author data is {}", userEntity);
        return userEntity;
    }

}
