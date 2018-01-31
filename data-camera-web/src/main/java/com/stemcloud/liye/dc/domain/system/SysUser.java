package com.stemcloud.liye.dc.domain.system;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/27
 */
@Entity
@Table(name = "dc_sys_user")
public class SysUser {
    @Id
    @Column(name = "user_name")
    private String user;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @OneToOne
    private SysRole role;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(updatable = false)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    @Column(updatable = false,
            columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date modifyTime;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SysRole getRole() {
        return role;
    }

    public void setRole(SysRole role) {
        this.role = role;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }
}
