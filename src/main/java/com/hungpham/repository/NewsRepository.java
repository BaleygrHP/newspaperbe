package com.hungpham.repository;

import com.hungpham.entity.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<NewsEntity,String> {
    List<NewsEntity> getByAuthor(String author);
    @Query(nativeQuery = true, value="SELECT n.id, n.title, n.img_new, n.short_description, u.user_nick_name, n.created_date, n.updated_date, c.is_main_topic, c.category_name \n" +
            "FROM newspaper n JOIN category c JOIN user u\n" +
            "ON n.category = c.id AND n.author = u.id\n" +
            "WHERE c.is_main_topic IN ('Main','Sub1','Sub2','Sub3') AND n.delete_flag = 0 ORDER BY n.created_date DESC;")
    List<Object[]> getDataBodyNews();
}
