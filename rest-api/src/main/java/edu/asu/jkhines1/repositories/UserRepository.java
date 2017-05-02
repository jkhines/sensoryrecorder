/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.repositories;

import edu.asu.jkhines1.models.User;

/**
 *
 * @author jkhines
 */
public interface UserRepository {
    
    public User getByUsername(String username);
    
    public User save(User user);
}
