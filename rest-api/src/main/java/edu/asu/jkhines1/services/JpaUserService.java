/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.asu.jkhines1.models.User;
import edu.asu.jkhines1.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jkhines
 */
@Service("userService")
@Transactional
public class JpaUserService implements UserService {
    
    @Autowired
    UserRepository userRepository;

    @Override
    public User getByUsername(String username) {
        return userRepository.getByUsername(username);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
