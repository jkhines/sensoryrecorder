/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.services;

import edu.asu.jkhines1.models.User;

/**
 *
 * @author jkhines
 */
public interface UserService {
    
    public User getByUsername(String username);
    
    public User save(User user);
}
