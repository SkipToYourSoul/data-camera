package com.stemcloud.liye.dc.domain.data;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/17
 */
@Entity
@Table(name = "dc_data_content_info")
public class ContentInfo {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    private String tag;

    private String img;

    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    private int view = 0;

    @Column(name = "comment_count", columnDefinition = "INT DEFAULT 0")
    private int comment = 0;

    @Column(name = "like_count", columnDefinition = "INT DEFAULT 0")
    private int like = 0;

    @Column(name = "is_shared", columnDefinition = "INT DEFAULT 0")
    private int isShared = 0;

    @Column(name = "is_deleted", columnDefinition = "INT DEFAULT 0")
    private int isDeleted = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @OneToOne
    private RecorderInfo recorderInfo;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getIsShared() {
        return isShared;
    }

    public void setIsShared(int isShared) {
        this.isShared = isShared;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public RecorderInfo getRecorderInfo() {
        return recorderInfo;
    }

    public void setRecorderInfo(RecorderInfo recorderInfo) {
        this.recorderInfo = recorderInfo;
    }
}
