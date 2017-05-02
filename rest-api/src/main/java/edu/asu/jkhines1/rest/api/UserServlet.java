/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.asu.jkhines1.rest.api;

import edu.asu.jkhines1.utils.TokenValidator;
import com.google.common.base.Strings;
import edu.asu.jkhines1.models.User;
import edu.asu.jkhines1.services.UserService;
import edu.asu.jkhines1.utils.Scope;
import edu.asu.jkhines1.utils.TokenInfo;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.validator.EmailValidator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 *
 * @author jkhines
 */
@WebServlet(urlPatterns = {"/users"})
public class UserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private static final EmailValidator emailValidator = EmailValidator.getInstance();

    @Autowired
    TokenValidator tokenValidator;
    
    @Autowired
    UserService userService;

    /**
     * Required to get Spring dependency injection working.
     * @throws javax.servlet.ServletException
     */
    @Override
    public void init() throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    //
    // It's technically possible to create an unlimited number of users
    // in the system. However, requiring a token makes doing this slightly
    // harder while providing a way to revoke access if it happens.
    //
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // Validate access token.
        TokenInfo tokenInfo;
        try {
            tokenInfo = tokenValidator.validate(request.getHeader("Authorization"), 
                    Scope.CREATE_USER_SCOPE);
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
            User user = mapper.readValue(request.getReader(), User.class);

            if (!isValid(user)) {
                logger.log(Level.WARNING, "Attempt to create an invalid user account.");

                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid user settings.");
                return;
            }
            
            // Check for an existing user.
            User savedUser = userService.getByUsername(user.getUsername());

            // Only save the user if one does not already exist.
            if (savedUser != null) {
                logger.log(Level.WARNING, "Attempt to create existing account: {0}", 
                        savedUser.getUsername());

                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid user settings.");
                return;
            }
            
            userService.save(user);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex); 

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Unexpected error.");
        }
    }

    // Check to ensure that the valid fields are set.
    private Boolean isValid(User user) {
        String username = user.getUsername();
        
        return (!Strings.isNullOrEmpty(username) &&
                !Strings.isNullOrEmpty(user.getPassword()) &&
                emailValidator.isValid(username) &&
                user.getEnabled());
    }
}
