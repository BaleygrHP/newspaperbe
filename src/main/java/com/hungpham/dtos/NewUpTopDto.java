package com.hungpham.dtos;

import com.hungpham.common.enums.TypeContentEnum;

public class NewUpTopDto extends AbstractDto<NewsDto>{
    private String title;
    private String categoryName;
    private String shortDescription;
    private String author;
    private String img_new;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public String getImg_new() {
        return img_new;
    }

    public void setImg_new(String img_new) {
        this.img_new = img_new;
    }
}
