/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import edu.asu.jkhines1.models.Device;
import edu.asu.jkhines1.repositories.DeviceRepository;

/**
 *
 * @author jkhines
 */
@Service("deviceService")
@Transactional
public class JpaDeviceService implements DeviceService {
    
    @Autowired
    DeviceRepository deviceRepository;

    @Override
    public Device getById(Long id) {
        return deviceRepository.getById(id);
    }

    @Override
    public List<Device> getByUsername(String username) {
        return deviceRepository.getByUsername(username);
    }

    @Override
    public Device getByUsernameAndClientId(String username, String clientId) {
        return deviceRepository.getByUsernameAndClientId(username, clientId);
    }

    @Override
    public Device save(Device device) {
        return deviceRepository.save(device);
    }
}
