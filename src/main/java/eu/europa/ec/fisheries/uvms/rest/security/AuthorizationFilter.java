package eu.europa.ec.fisheries.uvms.rest.security;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.mare.usm.information.domain.Context;
import eu.europa.ec.mare.usm.information.domain.Feature;
import eu.europa.ec.mare.usm.information.domain.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by georgige on 9/22/2015.
 */
public class AuthorizationFilter extends AbstractUSMHandler implements Filter{

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (super.getInfoService() == null) {
            throw new ServletException("InformationService is undefined");
        }
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.debug("AuthorizationFilter.doFilter(...) START");

        if (request instanceof UserRoleRequestWrapper) {
            Set<String> featuresStr = null;
            UserRoleRequestWrapper requestWrapper = (UserRoleRequestWrapper) request;
            UserContext userContext = getUserContext(requestWrapper.getRemoteUser(), getApplicationName(request.getServletContext()));

            String currentScope = requestWrapper.getHeader(AuthConstants.HTTP_HEADER_SCOPE_NAME); //get it from the header
            String currentRole = requestWrapper.getHeader(AuthConstants.HTTP_HEADER_ROLE_NAME); //get it from the header

            LOGGER.debug("Current requests is with scope '{}', and role '{}'", currentScope, currentRole);

            //we will use the session to cache the features/roles for the current scope/role selection
            HttpSession session = requestWrapper.getSession(true);

            //check if current scope and role have been changed in the meanwhile
            if (!isEqualToSessionAttr(session, HTTP_SESSION_ATTR_ROLE_NAME, currentRole) || !isEqualToSessionAttr(session, HTTP_SESSION_ATTR_SCOPE_NAME, currentScope) ) {
                //then try to get the new selection and set the features as user roles (for the particular Application only)
                for (Context usmCtx: userContext.getContextSet().getContexts()) {

                    if (usmCtx.getRole().getRoleName().equalsIgnoreCase(currentRole) && usmCtx.getScope().getScopeName().equalsIgnoreCase(currentScope)) {
                        Set<Feature> features = usmCtx.getRole().getFeatures();
                        featuresStr = new HashSet<>(features.size());
                        String appName = getApplicationName(request.getServletContext());

                        //extract only the features that the particular application is interested in
                        for(Feature feature: features) {
                            if (appName.equalsIgnoreCase(feature.getApplicationName())) {
                                featuresStr.add(feature.getFeatureName());
                            }
                        }

                        //caching the roles and scope
                        session.setAttribute(HTTP_SESSION_ATTR_ROLES_NAME, featuresStr);
                        session.setAttribute(HTTP_SESSION_ATTR_ROLE_NAME, currentRole);
                        session.setAttribute(HTTP_SESSION_ATTR_SCOPE_NAME, currentScope);
                    }

                    break;
                }

                if (featuresStr == null) {
                    LOGGER.warn("Unauthorized attempt to access resource with scope '{}' and role '{}', which don't exist for the current user.", currentScope, currentRole);
                }
            } else {
                featuresStr = (Set<String>) session.getAttribute(HTTP_SESSION_ATTR_ROLES_NAME);
            }

            if (featuresStr != null) {
                requestWrapper.setRoles(featuresStr);
            }
        }

        LOGGER.debug("AuthorizationFilter.doFilter(...) END");
        chain.doFilter(request,response);
    }


    protected boolean isEqualToSessionAttr(HttpSession session,  String attrName, String attrValue) {
        boolean isEqual = false;

        Object attr = session.getAttribute(attrName);
        if (attr != null && attr.toString().equalsIgnoreCase(attrValue)) {
            isEqual = true;
        }

        return isEqual;
    }

    @Override
    public void destroy() {

    }
}
