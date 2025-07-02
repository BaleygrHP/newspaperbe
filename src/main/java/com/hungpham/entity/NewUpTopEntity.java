package com.hungpham.entity;


import com.hungpham.common.enums.TypeContentEnum;

import javax.persistence.*;

@Entity
@Table(name = "top_newspaper")
public class NewUpTopEntity extends baseEntity{


    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "category")
    private CategoryEntity category;
    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "img_new")
    private String imageNew;

    @OneToOne
    @JoinColumn(name = "author")
    private UserEntity author;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getImageNew() {
        return imageNew;
    }

    public void setImageNew(String imageNew) {
        this.imageNew = imageNew;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

}
