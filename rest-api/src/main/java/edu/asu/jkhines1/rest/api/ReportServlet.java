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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jkhines
 */
@WebServlet(urlPatterns = {"/reports"})
public class ReportServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ReportServlet.class.getName());
    
    @Autowired
    TokenValidator tokenValidator;

    @Autowired
    InputDataService inputDataService;

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

        // Check QueryString parameters for type of data to return.
        String filter = request.getParameter("filter");
        String duration = request.getParameter("duration");
        
        if (Strings.isNullOrEmpty(filter) || Strings.isNullOrEmpty(duration) ||
                (!duration.equalsIgnoreCase("week") && !duration.equalsIgnoreCase("month"))) {
            logger.log(Level.WARNING, "Invalid QueryString.");
            
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid QueryString.");
            return;            
        }
        
        LocalDate currentDate = LocalDate.now();
        Date startDate;
        switch (duration.toLowerCase()) {
            case "week":
                startDate = Date.from(currentDate.minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
            case "month":
                startDate = Date.from(currentDate.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
            default:
                startDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
        }

        List<InputData> inputData = null;
        try {
            switch (filter.toLowerCase()) {
                case "behavior":
                    inputData = inputDataService.getBehavior(username, startDate);
                    break;
                case "rest":
                    inputData = inputDataService.getRest(username, startDate);
                    break;
                case "sound":
                    inputData = inputDataService.getSound(username, startDate);
                    break;
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
}
