package com.hungpham.entity;

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

    @Column(name = "short_description")
    private String shortDescription;

    @OneToOne
    @JoinColumn(name = "author")
    private UserEntity author;

    @Column(name = "delete_flag")
    private Boolean deleteFlag;


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

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}
