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
@JsonDeserialize(as=JpaInputData.class)
public interface InputData extends Serializable {

    @JsonProperty("id")
    public Long getId();

    public void setId(Long id);
    
    @JsonProperty("username")
    public String getUsername();

    public void setUsername(String username);

    @JsonProperty("type")
    public String getType();

    public void setType(String type);

    @JsonProperty("quantity")
    public Long getQuantity();

    public void setQuantity(Long quantity);

    @JsonProperty("recorded_at")
    public Date getRecordedAt();

    public void setRecordedAt(Date recordedAt);
}
