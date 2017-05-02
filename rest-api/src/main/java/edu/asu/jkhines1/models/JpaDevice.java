/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.models;

import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author jkhines
 */
@Entity
@Table(name="devices")
@NamedQueries({
    @NamedQuery(name=JpaDevice.QUERY_ALL, query = 
            "SELECT d FROM JpaDevice d"),
    @NamedQuery(name=JpaDevice.QUERY_BY_ID, query = 
            "SELECT d FROM JpaDevice d WHERE d.id = :" + JpaDevice.PARAM_ID),
    @NamedQuery(name=JpaDevice.QUERY_BY_USERNAME, query = 
            "SELECT d FROM JpaDevice d WHERE d.username = :" + JpaDevice.PARAM_USERNAME +
                    " ORDER BY d.recordedAt"),
    @NamedQuery(name=JpaDevice.QUERY_BY_USERNAME_AND_CLIENT_ID, query = 
            "SELECT d FROM JpaDevice d WHERE d.username = :" + JpaDevice.PARAM_USERNAME + 
                    " AND d.clientId = :" + JpaDevice.PARAM_CLIENT_ID)
})
public class JpaDevice implements Device {
    
    private static final long serialVersionUID = 1L;
    
    public static final String QUERY_ALL = "JpaDevice.getAll";
    public static final String QUERY_BY_ID = "JpaDevice.getById";
    public static final String QUERY_BY_USERNAME = "JpaDevice.getByUsername";
    public static final String QUERY_BY_USERNAME_AND_CLIENT_ID = "JpaDevice.getByUsernameAndClientId";
    public static final String PARAM_ID = "id";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_CLIENT_ID = "client_id";
    private Long id;
    private String clientId;
    private String clientName;
    private String username;
    private Boolean approved;
    private Date recordedAt;
    
    public JpaDevice() {
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    @Basic
    @Column(name="client_id", nullable = false)
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    @Basic
    @Column(name="client_name", nullable = false)
    public String getClientName() {
        return clientName;
    }

    @Override
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    @Override
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
    @Column(name="approved", nullable = false)
    public Boolean getApproved() {
        return approved;
    }

    @Override
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @Override
    @Basic
    @Column(name="recorded_at")
    public Date getRecordedAt() {
        return recordedAt;
    }

    @Override
    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }
}
