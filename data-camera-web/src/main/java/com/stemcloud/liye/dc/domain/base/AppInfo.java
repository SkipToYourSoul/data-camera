package com.stemcloud.liye.dc.domain.base;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *  base app info
 * @author liye
 */
@Entity
@Table(name = "dc_base_app_info")
public class AppInfo {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "app_name", nullable = false)
    private String name;

    @Column(name = "app_creator", nullable = false)
    private String creator;

    @Column(name = "app_description")
    private String description;

    @Column(name = "is_deleted",
            columnDefinition = "INT DEFAULT 0")
    private int isDeleted = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    @Column(name = "modify_time",
            updatable = false,
            columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date modifyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creator='" + creator + '\'' +
                ", description='" + description + '\'' +
                ", isDeleted=" + isDeleted +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
