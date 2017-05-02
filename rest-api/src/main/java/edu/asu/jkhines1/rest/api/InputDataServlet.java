/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.rest.api;

import edu.asu.jkhines1.utils.TokenValidator;
import com.google.common.base.Strings;
import com.google.json.JsonSanitizer;
import edu.asu.jkhines1.models.InputData;
import edu.asu.jkhines1.services.InputDataService;
import edu.asu.jkhines1.utils.Scope;
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
@WebServlet(urlPatterns = {"/data"})
public class InputDataServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(InputDataServlet.class.getName());
    private static final EmailValidator emailValidator = EmailValidator.getInstance();

    public InputDataServlet() {
    }
    
    @Autowired
    TokenValidator tokenValidator;

    @Autowired
    InputDataService inputDataService;

    @Autowired
    DeviceService deviceService;
    
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
                    Scope.READ_DATA_SCOPE);
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

        // Make sure the user is asking for their own data
        if (!username.equals(tokenInfo.getUserId())) {
            return;
        }

        List<InputData> inputData = null;
        try {
            // Check QueryString parameters for type of data to return.
            String filter = request.getParameter("filter");
            if (!Strings.isNullOrEmpty(filter)) {
                if (filter.equals("latest")) {
                    inputData = inputDataService.getLatestByUsername(username);
                } else {
                    inputData = inputDataService.getByUsernameAndType(username, filter);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error.");
            return;
        }

        if (inputData != null && !inputData.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();

            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println(JsonSanitizer.sanitize(mapper.writeValueAsString(inputData)));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // Validate access token.
        TokenInfo tokenInfo;
        try {
            tokenInfo = tokenValidator.validate(request.getHeader("Authorization"), 
                    Scope.CREATE_DATA_SCOPE);
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
            InputData inputData = mapper.readValue(request.getReader(), InputData.class);
            
            if (!isValid(inputData)) {
                logger.log(Level.WARNING, "Invalid data values.");

                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid data values.");
                return;
            }
            
            // If this is coming from the mobile app, make sure
            // the submitter is submitting for themselves.
            if (!Strings.isNullOrEmpty(tokenInfo.getUserId())) {
                if (!inputData.getUsername().equals(tokenInfo.getUserId())) {
                    return;
                }
                
            // If this is coming from an IoT device, make sure the device
            // was approved by the user whose data is being submitted.
            } else {
                Device savedDevice = deviceService.getByUsernameAndClientId(
                        inputData.getUsername(), tokenInfo.getClientId());
                
                if (savedDevice == null || !savedDevice.getApproved())
                    return;
            }
            
            inputDataService.save(inputData);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected error.");
        }
    }

    // Check to ensure that the valid fields are set.
    private Boolean isValid(InputData inputData) {
        String username = inputData.getUsername();
        String type = inputData.getType();
        
        return (!Strings.isNullOrEmpty(username) &&
                !Strings.isNullOrEmpty(type) &&
                emailValidator.isValid(username) &&
                (type.equals("sound") || type.equals("sleep") || type.equals("wake") || type.equals("behavior")));
    }
}
