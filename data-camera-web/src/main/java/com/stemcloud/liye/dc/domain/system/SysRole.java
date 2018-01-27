package com.stemcloud.liye.dc.domain.system;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/27
 */
@Entity
@Table(name = "dc_sys_role")
public class SysRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany(cascade = {CascadeType.REFRESH},fetch = FetchType.EAGER)
    private Set<SysResource> sysResources = new HashSet<SysResource>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SysResource> getSysResources() {
        return sysResources;
    }

    public void setSysResources(Set<SysResource> sysResources) {
        this.sysResources = sysResources;
    }
}
