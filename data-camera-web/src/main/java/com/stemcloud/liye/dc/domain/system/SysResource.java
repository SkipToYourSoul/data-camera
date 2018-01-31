package com.stemcloud.liye.dc.domain.system;

import javax.persistence.*;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/27
 */
@Entity
@Table(name = "dc_sys_resource")
public class SysResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String resourceUrl;

    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
