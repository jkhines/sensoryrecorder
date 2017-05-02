/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.asu.jkhines1.models.InputData;
import edu.asu.jkhines1.models.JpaInputData;
import edu.asu.jkhines1.repositories.InputDataRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jkhines
 */
@Service("inputDataService")
@Transactional
public class JpaInputDataService implements InputDataService {
    
    @Autowired
    InputDataRepository dataRepository;

    @Override
    public InputData getById(Long id) {
        return dataRepository.getById(id);
    }

    @Override
    public List<InputData> getBehavior(String username, Date startDate) {
        return dataRepository.getBehavior(username, startDate);
    }
    
    @Override
    public List<InputData> getByUsernameAndType(String username, String type) {
        List<InputData> inputData = dataRepository.getByUsernameAndType(username, type);
        if (inputData != null) {
            Collections.reverse(inputData);
        }
        return inputData;
    }

    @Override
    public List<InputData> getLatestByUsername(String username) {
        return dataRepository.getLatestByUsername(username);
    }

    @Override
    public List<InputData> getRest(String username, Date startDate) {
        List<InputData> sleepAndWakeData = dataRepository.getRest(username, startDate);
        List<InputData> restData = new ArrayList<>();

        // Convert the alternating sleep/wake entries into rest entries
        // containing the minutes of sleep.
        Date lastSleep = null;
        Date lastWake;
        for (InputData data : sleepAndWakeData) {
            if (data.getType().equals("sleep")) {
                lastSleep = data.getRecordedAt();
            } else if (lastSleep != null) {
                lastWake = data.getRecordedAt();
                long minutesOfSleep = TimeUnit.MILLISECONDS.toMinutes(
                        lastWake.getTime() - lastSleep.getTime());
                
                InputData rest = new JpaInputData();

                rest.setId(data.getId());
                rest.setUsername(data.getUsername());
                rest.setType("rest");
                rest.setQuantity(minutesOfSleep);
                rest.setRecordedAt(data.getRecordedAt());
                restData.add(rest);
            }
        }
        return restData;
    }

    @Override
    public List<InputData> getSound(String username, Date startDate) {
        return dataRepository.getSound(username, startDate);
    }

    @Override
    public InputData save(InputData inputData) {
        return dataRepository.save(inputData);
    }
}
