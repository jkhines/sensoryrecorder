/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.utils;

/**
 *
 * @author jkhines
 */
public enum Scope {
    CREATE_DEVICE_SCOPE("dssr:device:create"),
    READ_DEVICE_SCOPE("dssr:device:read"),
    UPDATE_DEVICE_SCOPE("dssr:device:update"),
    CREATE_DATA_SCOPE("dssr:data:create"),
    READ_DATA_SCOPE("dssr:data:read"),
    CREATE_USER_SCOPE("dssr:user:create");

    private final String text;

    /**
     * @param text
     */
    private Scope(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }   
}
