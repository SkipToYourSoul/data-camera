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

    private String owner;

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
