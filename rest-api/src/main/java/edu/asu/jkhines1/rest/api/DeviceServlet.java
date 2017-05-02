/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.rest.api;

import edu.asu.jkhines1.utils.Scope;
import edu.asu.jkhines1.utils.TokenValidator;
import com.google.common.base.Strings;
import com.google.json.JsonSanitizer;
import edu.asu.jkhines1.services.UserService;
import edu.asu.jkhines1.utils.TokenInfo;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import edu.asu.jkhines1.models.Device;
import edu.asu.jkhines1.services.DeviceService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.validator.EmailValidator;

/**
 *
 * @author jkhines
 */
@WebServlet(urlPatterns = {"/devices"})
public class DeviceServlet extends HttpServlet {
    private static final int MAX_DEVICES_ALLOWED = 6;
    private static final Logger logger = Logger.getLogger(DeviceServlet.class.getName());
    private static final EmailValidator emailValidator = EmailValidator.getInstance();
    
    @Autowired
    TokenValidator tokenValidator;

    @Autowired
    DeviceService deviceService;

    @Autowired
    UserService userService;
    
    /**
     * Required to get Spring dependency injection working.
     *
     * @throws javax.servlet.ServletException
     */
    @Override
    public void init() throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Validate access token.
        TokenInfo tokenInfo;
        try {
            tokenInfo = tokenValidator.validate(request.getHeader("Authorization"), 
                    Scope.READ_DEVICE_SCOPE);
        } catch (IllegalArgumentException ex) {
            logger.log(Level.WARNING, null, ex);
            
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                    "Bad or missing access token.");
            return;            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied.");
            return;
        }
        
        // Get the username from the URL.
        String username = request.getPathInfo();
        if (username != null) {
            username = username.replaceFirst("/", "");
        }
        if (Strings.isNullOrEmpty(username)) {
            return;
        }

        // Make sure the user is asking for their own data.
        if (!username.equals(tokenInfo.getUserId())) {
            return;
        }
            
        List<Device> devices = null;
        try {
            devices = deviceService.getByUsername(username);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error.");
            return;
        }

        if (devices != null && !devices.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();

            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println(JsonSanitizer.sanitize(mapper.writeValueAsString(devices)));
            }
        }
    }

    //
    // The nature of this endpoint is such that any dynamically registered client
    // could potentially request to be approved by every user in the system.
    // For this reason, error codes are not returned if the requested user
    // does not exist in the system, nor if a device already exists.
    // This makes it impossible to submit devices using a list of email addresses
    // and use error codes to determine which users exist in the system.
    //
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // Validate access token.
        TokenInfo tokenInfo;
        try {
            tokenInfo = tokenValidator.validate(request.getHeader("Authorization"), 
                    Scope.CREATE_DEVICE_SCOPE);
        } catch (IllegalArgumentException ex) {
            logger.log(Level.WARNING, null, ex);

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                    "Bad or missing access token.");
            return;            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied.");
            return;
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            Device device = mapper.readValue(request.getReader(), Device.class);
            device.setApproved(false);
            
            if (!isValid(device)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid device values.");
                return;
            }

            // Make sure this requested device is being approved.
            if (!device.getClientId().equals(tokenInfo.getClientId())) {
                return;
            }

            // Make sure that the approver exists.
            // Leave out for now. Might plug in the IoT device before
            // registering an account via the mobile app.
            //User savedUser = userService.getByUsername(device.getUsername());
            //if (savedUser == null) {
            //    return;
            //}
            
            // Check for an existing device.
            Device savedDevice = deviceService.getByUsernameAndClientId(
                    device.getUsername(), device.getClientId());

            // Only save the device if one does not already exist.
            if (savedDevice != null) {
                return;
            }
            
            // Check for the maximum number of devices.
            List<Device> devices = deviceService.getByUsername(device.getUsername());
            if (devices.size() >= MAX_DEVICES_ALLOWED) {
                logger.log(Level.WARNING, "User {0} has reached the maximum number of devices [{1}].", 
                        new Object[]{device.getUsername(), MAX_DEVICES_ALLOWED});

                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                        "User has reached the maximum number of devices [" + MAX_DEVICES_ALLOWED + "].");
                return;                
            }
            
            deviceService.save(device);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // Validate access token.
        TokenInfo tokenInfo;
        try {
            tokenInfo = tokenValidator.validate(request.getHeader("Authorization"), 
                    Scope.UPDATE_DEVICE_SCOPE);
        } catch (IllegalArgumentException ex) {
            logger.log(Level.WARNING, null, ex);

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                    "Bad or missing access token.");
            return;            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied.");
            return;
        }
        
        ObjectMapper mapper = new ObjectMapper();
        Device device, savedDevice;

        try {
            device = mapper.readValue(request.getReader(), Device.class);
            
            if (!isValid(device)) {
                logger.log(Level.WARNING, "Invalid device values.");

                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid device values.");
                return;
            }

            // Make sure the user is approving their own device.
            if (!device.getUsername().equals(tokenInfo.getUserId())) {
                logger.log(Level.SEVERE, "Attempt to overwrite data by {0} for {1}", 
                        new Object[]{tokenInfo.getUserId(), device.getUsername()});

                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid device values.");
                return;
            }
            
            // Check for an existing device.
            savedDevice = deviceService.getById(device.getId());

            // Only update the device if one exists.
            if (savedDevice == null) {
                logger.log(Level.WARNING, "Attempt to update existing device: {0}", 
                        savedDevice.getClientName());

                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid device values.");
                return;                    
            }

            deviceService.save(device);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error.");
        }
    }

    // Check to ensure that the valid fields are set.
    // Check to ensure that the valid fields are set.
    private Boolean isValid(Device device) {
        String username = device.getUsername();
        
        return (!Strings.isNullOrEmpty(username) &&
                !Strings.isNullOrEmpty(device.getClientId()) &&
                !Strings.isNullOrEmpty(device.getClientName()) &&
                device.getApproved() != null &&
                emailValidator.isValid(username));
    }
}
