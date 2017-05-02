/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.repositories;

import edu.asu.jkhines1.models.InputData;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jkhines
 */
public interface InputDataRepository {
    
    public List<InputData> getBehavior(String username, Date startDate);
    
    public InputData getById(Long id);

    public List<InputData> getByUsernameAndType(String username, String type);

    public List<InputData> getLatestByUsername(String username);
    
    public List<InputData> getRest(String username, Date startDate);
    
    public List<InputData> getSound(String username, Date startDate);

    public InputData save(InputData inputData);
}
