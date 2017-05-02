/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.models;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author jkhines
 */
@JsonDeserialize(as=JpaDevice.class)
public interface Device extends Serializable {

    @JsonProperty("id")
    public Long getId();

    public void setId(Long id);

    @JsonProperty("client_id")
    public String getClientId();

    public void setClientId(String clientId);

    @JsonProperty("client_name")
    public String getClientName();

    public void setClientName(String clientName);
    
    @JsonProperty("username")
    public String getUsername();

    public void setUsername(String username);

    @JsonProperty("approved")
    public Boolean getApproved();

    public void setApproved(Boolean enabled);

    @JsonProperty("recorded_at")
    public Date getRecordedAt();

    public void setRecordedAt(Date recordedAt);
}
