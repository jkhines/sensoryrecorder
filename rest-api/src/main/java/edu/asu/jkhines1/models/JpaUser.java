/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.models;

import javax.persistence.*;

/**
 *
 * @author jkhines
 */
@Entity
@Table(name="users")
@NamedQueries({
    @NamedQuery(name=JpaUser.QUERY_ALL, query = 
            "SELECT u FROM JpaUser u"),
    @NamedQuery(name=JpaUser.QUERY_BY_USERNAME, query = 
            "SELECT u FROM JpaUser u WHERE u.username = :" + JpaUser.PARAM_USERNAME)
})
public class JpaUser implements User {
    
    private static final long serialVersionUID = 1L;
    
    public static final String QUERY_ALL = "JpaUser.getAll";
    public static final String QUERY_BY_USERNAME = "JpaUser.getByUsername";
    public static final String PARAM_USERNAME = "username";
    private String username;
    private String password;
    private Boolean enabled;
    
    public JpaUser() {
    }
    
    @Override
    @Id
    @Basic
    @Column(name="username", nullable = false)
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    @Basic
    @Column(name="password", nullable = false)
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    @Basic
    @Column(name="enabled", nullable = false)
    public Boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
