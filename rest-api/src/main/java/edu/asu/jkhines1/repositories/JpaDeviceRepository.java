/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.repositories;

import edu.asu.jkhines1.models.JpaDevice;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import edu.asu.jkhines1.models.Device;

/**
 *
 * @author jkhines
 */
@Repository("deviceRepository")
public class JpaDeviceRepository implements DeviceRepository {

    @PersistenceContext(unitName="apiPersistenceUnit")
    private EntityManager manager;

    @Override
    @Transactional(value="apiTransactionManager")
    public Device getById(Long id) {
        TypedQuery<Device> query = manager.createNamedQuery(JpaDevice.QUERY_BY_ID, Device.class);
        query.setParameter(JpaDevice.PARAM_ID, id);
        List<Device> results = query.getResultList();
        return (results.size() > 0) ? results.get(0) : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public List<Device> getByUsername(String username) {
        TypedQuery<Device> query = manager.createNamedQuery(JpaDevice.QUERY_BY_USERNAME, Device.class);
        query.setParameter(JpaDevice.PARAM_USERNAME, username);
        List<Device> results = query.getResultList();
        return (results.size() > 0) ? results : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public Device getByUsernameAndClientId(String username, String clientId) {
        TypedQuery<Device> query = manager.createNamedQuery(JpaDevice.QUERY_BY_USERNAME_AND_CLIENT_ID, Device.class);
        query.setParameter(JpaDevice.PARAM_USERNAME, username);
        query.setParameter(JpaDevice.PARAM_CLIENT_ID, clientId);
        List<Device> results = query.getResultList();
        return (results.size() > 0) ? results.get(0) : null;
    }
    
    @Override
    @Transactional(value="apiTransactionManager")
    public Device save(Device device) {
        Device savedDevice = manager.merge(device);
        manager.flush();
        return savedDevice;
    }
}
