/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.models;

import java.io.Serializable;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author jkhines
 */
@JsonDeserialize(as=JpaUser.class)
public interface User extends Serializable {

    @JsonProperty("username")
    public String getUsername();

    public void setUsername(String username);

    @JsonIgnore
    public String getPassword();

    @JsonProperty("password")
    public void setPassword(String password);

    @JsonProperty("enabled")
    public Boolean getEnabled();

    public void setEnabled(Boolean enabled);
}
