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
@Table(name="input_data")
@NamedQueries({
    @NamedQuery(name=JpaInputData.QUERY_ALL, query = 
            "SELECT d FROM JpaInputData d ORDER BY d.recordedAt"),
    @NamedQuery(name=JpaInputData.QUERY_BY_ID, query = 
            "SELECT d FROM JpaInputData d WHERE d.id = :" + JpaInputData.PARAM_ID +
                    " ORDER BY d.recordedAt"),
    @NamedQuery(name=JpaInputData.QUERY_BEHAVIOR_BY_RECORDED_AT, query = 
            "SELECT d FROM JpaInputData d WHERE d.username = :" +
                    JpaInputData.PARAM_USERNAME + 
                    " AND d.type = 'behavior' AND d.recordedAt > :" + 
                    JpaInputData.PARAM_RECORDED_AT +
                    " ORDER BY d.recordedAt"),
    @NamedQuery(name=JpaInputData.QUERY_BY_USERNAME_AND_TYPE, query = 
            "SELECT d FROM JpaInputData d WHERE d.username = :" + JpaInputData.PARAM_USERNAME +
                    " AND d.type = :" + JpaInputData.PARAM_TYPE +
                    " ORDER BY d.recordedAt DESC"),
    @NamedQuery(name=JpaInputData.QUERY_REST_BY_RECORDED_AT, query = 
            "SELECT d FROM JpaInputData d WHERE d.username = :" + JpaInputData.PARAM_USERNAME + 
                    " AND (d.type = 'wake' OR d.type = 'sleep') AND d.recordedAt > :" + 
                    JpaInputData.PARAM_RECORDED_AT +
                    " ORDER BY d.recordedAt"),
})
@NamedNativeQueries({
    @NamedNativeQuery(name=JpaInputData.QUERY_LATEST_BY_USERNAME, query = 
            "SELECT * FROM (SELECT *, ROW_NUMBER() OVER" + 
                    " (PARTITION BY type ORDER BY recorded_at DESC) rn" +
                    " FROM input_data WHERE username = ?) tmp WHERE rn = 1",
        resultClass = JpaInputData.class),
    @NamedNativeQuery(name=JpaInputData.QUERY_SOUND_BY_RECORDED_AT, query = 
            "SELECT date_trunc('day', recorded_at) id, ? username, type, AVG(quantity) quantity, date_trunc('day', recorded_at) recorded_at" +
                    " FROM input_data WHERE type = 'sound' AND username = ? and recorded_at > ?" +
                    " GROUP BY type, date_trunc('day', recorded_at)" +
                    " ORDER BY 1",
        resultClass = JpaInputData.class),
})
public class JpaInputData implements InputData {
    
    private static final long serialVersionUID = 1L;
    
    public static final String QUERY_ALL = "JpaInputData.getAll";
    public static final String QUERY_BEHAVIOR_BY_RECORDED_AT = "JpaInputData.getBehaviorByRecordedAt";
    public static final String QUERY_BY_ID = "JpaInputData.getById";
    public static final String QUERY_BY_USERNAME_AND_TYPE = "JpaInputData.getByUsernameAndType";
    public static final String QUERY_LATEST_BY_USERNAME = "JpaInputData.getLatestByUsername";
    public static final String QUERY_REST_BY_RECORDED_AT = "JpaInputData.getRestByRecordedAt";
    public static final String QUERY_SOUND_BY_RECORDED_AT = "JpaInputData.getSoundByRecordedAt";
    public static final String PARAM_RECORDED_AT = "recorded_at";
    public static final String PARAM_ID = "id";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_TYPE = "type";
    private Long id;
    private String username;
    private String type;
    private Long quantity;
    private Date recordedAt;
    
    public JpaInputData() {
    }
    
    public JpaInputData(Long id, String username, String type, Long quantity, Date recordedAt) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.quantity = quantity;
        this.recordedAt = recordedAt;
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
    @Column(name="type", nullable = false)
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    @Basic
    @Column(name="quantity")
    public Long getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
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
