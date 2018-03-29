package com.stemcloud.liye.dc.domain.data;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_data_recorder_info
 * @author liye on 2017/11/22
 */
@Entity
@Table(name = "dc_data_recorder_info")
public class RecorderInfo {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "exp_id", nullable = false)
    private long expId;

    @Column(name = "app_id", nullable = false)
    private long appId;

    @Column(name = "devices", nullable = false)
    private String devices;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "is_recorder",
            columnDefinition = "INT DEFAULT 1")
    private int isRecorder = 1;

    @Column(name = "is_deleted",
            columnDefinition = "INT DEFAULT 0")
    private int isDeleted = 0;

    @Column(name = "is_user_generate", columnDefinition = "INT DEFAULT 0")
    private int isUserGen = 0;

    @Column(name = "parent_id", columnDefinition = "INT DEFAULT -1")
    private long parentId = -1;

    @Column(name = "start_seconds", columnDefinition = "INT DEFAULT 0")
    private long startSeconds = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExpId() {
        return expId;
    }

    public void setExpId(long expId) {
        this.expId = expId;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getDevices() {
        return devices;
    }

    public void setDevices(String devices) {
        this.devices = devices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getIsRecorder() {
        return isRecorder;
    }

    public void setIsRecorder(int isRecorder) {
        this.isRecorder = isRecorder;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public int getIsUserGen() {
        return isUserGen;
    }

    public void setIsUserGen(int isUserGen) {
        this.isUserGen = isUserGen;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getStartSeconds() {
        return startSeconds;
    }

    public void setStartSeconds(long startSeconds) {
        this.startSeconds = startSeconds;
    }
}
