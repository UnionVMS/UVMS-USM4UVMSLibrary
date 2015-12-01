package eu.europa.ec.fisheries.uvms.rest.security;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMServiceBean;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.utils.SecuritySessionUtils;
import eu.europa.ec.fisheries.wsdl.user.types.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by georgige on 9/22/2015.
 */
public class AuthorizationFilter extends AbstractUSMHandler implements Filter, AuthConstants{



    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException {
        LOGGER.debug("AuthorizationFilter.doFilter(...) START");

        if (request instanceof UserRoleRequestWrapper) {
            UserRoleRequestWrapper requestWrapper = (UserRoleRequestWrapper) request;
            Set<String> featuresStr = null;

            String cacheKey = generateCacheKey(requestWrapper);
            String applicationName = getApplicationName(request.getServletContext());
            String currentScope = requestWrapper.getHeader(HTTP_HEADER_SCOPE_NAME); //get it from the header
            String currentRole = requestWrapper.getHeader(HTTP_HEADER_ROLE_NAME); //get it from the header

            LOGGER.debug("Current requests is with scope '{}', and role '{}'", currentScope, currentRole);

            try {
                Context userContext = usmService.getUserContext(requestWrapper.getRemoteUser(), applicationName, currentRole, currentScope,cacheKey);

                if (userContext == null) {
                    ((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    featuresStr = usmService.getUserFeatures(requestWrapper.getRemoteUser(), userContext);
                }
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (featuresStr == null) {
                LOGGER.warn("Unauthorized attempt to access resource with scope '{}' and role '{}', which don't exist for the current user.", currentScope, currentRole);
            } else {
                requestWrapper.setRoles(featuresStr);
            }

        }

        LOGGER.debug("END AuthorizationFilter.doFilter(...)");

        try {
            chain.doFilter(request,response);
        } catch (IOException e) {
            LOGGER.error("failed to call WebFilter chain.doFilter(request,response). Caused by: {}", e.getMessage());
        }
    }


    @Override
    public void destroy() {
    }
}
