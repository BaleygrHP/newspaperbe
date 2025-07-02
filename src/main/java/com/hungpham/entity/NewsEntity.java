package com.hungpham.entity;

import com.hungpham.common.enums.TypeContentEnum;

import javax.persistence.*;


@Entity
@Table(name = "newspaper")
public class NewsEntity  extends baseEntity{
    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "category")
    private CategoryEntity category;

    @Lob  // Sử dụng Large Object để lưu HTML dài
    @Column(name = "content")
    private String content;

    @Column(name = "img_new")
    private String imageNew;

    @Column(name = "short_description")
    private String shortDescription;

    @JoinColumn(name = "author")
    private String author;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_content")
    private TypeContentEnum typeContent;

    @Column(name = "delete_flag")
    private Boolean deleteFlag;

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

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getImageNew() {
        return imageNew;
    }

    public void setImageNew(String imageNew) {
        this.imageNew = imageNew;
    }
}
