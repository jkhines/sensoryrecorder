/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.repositories;

import edu.asu.jkhines1.models.JpaUser;
import edu.asu.jkhines1.models.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jkhines
 */
@Repository("userRepository")
public class JpaUserRepository implements UserRepository {

    @PersistenceContext(unitName="authPersistenceUnit")
    private EntityManager manager;

    @Override
    @Transactional(value="authTransactionManager")
    public User getByUsername(String username) {
        TypedQuery<User> query = manager.createNamedQuery(JpaUser.QUERY_BY_USERNAME, User.class);
        query.setParameter(JpaUser.PARAM_USERNAME, username);
        List<User> results = query.getResultList();
        return (results.size() > 0) ? results.get(0) : null;
    }
    
    @Override
    @Transactional(value="authTransactionManager")
    public User save(User user) {
        User savedUser = manager.merge(user);
        manager.flush();
        return savedUser;
    }
}
