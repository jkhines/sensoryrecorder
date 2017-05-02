package edu.asu.jkhines1.dssr.models;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jkhines on 3/3/17.
 */

public class InputData {
    private Long id;
    private String username;
    private String type;
    private Long quantity;
    private Date recordedAt;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("quantity")
    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    @JsonProperty("recorded_at")
    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }
}
