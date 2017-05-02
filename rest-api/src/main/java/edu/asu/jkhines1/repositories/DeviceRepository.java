/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.repositories;

import java.util.List;
import edu.asu.jkhines1.models.Device;

/**
 *
 * @author jkhines
 */
public interface DeviceRepository {
    
    public Device getById(Long id);

    public List<Device> getByUsername(String username);

    public Device getByUsernameAndClientId(String username, String clientId);
    
    public Device save(Device device);
}
