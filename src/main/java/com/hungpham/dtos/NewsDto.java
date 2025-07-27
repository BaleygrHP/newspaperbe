package com.hungpham.dtos;

import com.hungpham.common.enums.TypeContentEnum;

public class NewsDto extends AbstractDto<NewsDto> {
    private String title;
    private String categoryName;
    private String content;
    private String shortDescription;
    private String imageNew;
    private String author;
    private Boolean deleteFlag;
    private TypeContentEnum typeContent;

    public TypeContentEnum getTypeContent() {
        return typeContent;
    }

    public void setTypeContent(TypeContentEnum typeContent) {
        this.typeContent = typeContent;
    }




    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageNew() {
        return imageNew;
    }

    public void setImageNew(String imageNew) {
        this.imageNew = imageNew;
    }
}
