/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.repositories;

import edu.asu.jkhines1.models.JpaInputData;
import edu.asu.jkhines1.models.InputData;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jkhines
 */
@Repository("inputDataRepository")
public class JpaInputDataRepository implements InputDataRepository {

    @PersistenceContext(unitName="apiPersistenceUnit")
    private EntityManager manager;

    @Override
    @Transactional(value="apiTransactionManager")
    public List<InputData> getBehavior(String username, Date startDate) {
        TypedQuery<InputData> query = manager.createNamedQuery(JpaInputData.QUERY_BEHAVIOR_BY_RECORDED_AT, InputData.class);
        query.setParameter(JpaInputData.PARAM_USERNAME, username);
        query.setParameter(JpaInputData.PARAM_RECORDED_AT, startDate);
        List<InputData> results = query.getResultList();
        return (results.size() > 0) ? results : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public InputData getById(Long id) {
        TypedQuery<InputData> query = manager.createNamedQuery(JpaInputData.QUERY_BY_ID, InputData.class);
        query.setParameter(JpaInputData.PARAM_ID, id);
        List<InputData> results = query.getResultList();
        return (results.size() > 0) ? results.get(0) : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public List<InputData> getByUsernameAndType(String username, String type) {
        TypedQuery<InputData> query = manager.createNamedQuery(JpaInputData.QUERY_BY_USERNAME_AND_TYPE, InputData.class);
        query.setParameter(JpaInputData.PARAM_USERNAME, username);
        query.setParameter(JpaInputData.PARAM_TYPE, type);
        query.setMaxResults(50);
        List<InputData> results = query.getResultList();
        return (results.size() > 0) ? results : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public List<InputData> getLatestByUsername(String username) {
        Query query = manager.createNamedQuery(JpaInputData.QUERY_LATEST_BY_USERNAME, InputData.class);
        query.setParameter(1, username);
        List<InputData> results = query.getResultList();
        return (results.size() > 0) ? results : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public List<InputData> getRest(String username, Date startDate) {
        TypedQuery<InputData> query = manager.createNamedQuery(JpaInputData.QUERY_REST_BY_RECORDED_AT, InputData.class);
        query.setParameter(JpaInputData.PARAM_USERNAME, username);
        query.setParameter(JpaInputData.PARAM_RECORDED_AT, startDate);
        List<InputData> results = query.getResultList();
        return (results.size() > 0) ? results : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public List<InputData> getSound(String username, Date startDate) {
        TypedQuery<InputData> query = manager.createNamedQuery(JpaInputData.QUERY_SOUND_BY_RECORDED_AT, InputData.class);
        query.setParameter(1, username);
        query.setParameter(2, username);
        query.setParameter(3, startDate);
        List<InputData> results = query.getResultList();
        return (results.size() > 0) ? results : null;
    }

    @Override
    @Transactional(value="apiTransactionManager")
    public InputData save(InputData inputData) {
        InputData savedInputData = manager.merge(inputData);
        manager.flush();
        return savedInputData;
    }
}
