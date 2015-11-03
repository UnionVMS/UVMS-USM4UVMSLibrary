package eu.europa.ec.fisheries.uvms.rest.security;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.wsdl.user.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

/**
 * Created by georgige on 9/22/2015.
 */
public class AuthorizationFilter extends AbstractUSMHandler implements Filter, AuthConstants{

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.debug("AuthorizationFilter.doFilter(...) START");

        if (request instanceof UserRoleRequestWrapper) {
            UserRoleRequestWrapper requestWrapper = (UserRoleRequestWrapper) request;

            String applicationName = getApplicationName(request.getServletContext());

            Set<String> featuresStr = null;
            Map<String, String> userPreferences = null;
            Map<String, List<Dataset>> userDatasets = null;

            String currentScope = requestWrapper.getHeader(HTTP_HEADER_SCOPE_NAME); //get it from the header
            String currentRole = requestWrapper.getHeader(HTTP_HEADER_ROLE_NAME); //get it from the header


            LOGGER.debug("Current requests is with scope '{}', and role '{}'", currentScope, currentRole);

            //we will use the session to cache the features/roles for the current scope/role selection
            HttpSession session = requestWrapper.getSession(true);

            //due to the fact the session object stays alive, no matter if the front-end jwtoken is invalidated/changed,
            // we need to check if the request is still coming from the same user!
            if (!session.isNew() && isSessionInvalid(session, requestWrapper.getRemoteUser(), currentRole, currentScope)) {
                session.invalidate();
                session = requestWrapper.getSession(true);
            }

            //check if current scope and role have been changed in the meanwhile
            if (session.isNew()) {
                try {
                    UserContext userContext = getUserContext(requestWrapper.getRemoteUser(), getApplicationName(request.getServletContext()));

                    if (userContext != null) {
                        //then try to get the new selection and set the features as user roles (for the particular Application only)
                        for (Context usmCtx : userContext.getContextSet().getContexts()) {

                            if (usmCtx.getRole().getRoleName().equalsIgnoreCase(currentRole) && usmCtx.getScope().getScopeName().equalsIgnoreCase(currentScope)) {
                                featuresStr = getFeaturesAsString(usmCtx, applicationName);
                                userPreferences = getUserPreferences(usmCtx, applicationName);
                                userDatasets = getCategorizedDatasets(usmCtx);

                                //caching the username, roles, scope, features, preferences and datasets
                                session.setAttribute(HTTP_SESSION_ATTR_ROLES_NAME, featuresStr);
                                session.setAttribute(HTTP_SESSION_ATTR_ROLE_NAME, currentRole);
                                session.setAttribute(HTTP_SESSION_ATTR_SCOPE_NAME, currentScope);
                                session.setAttribute(HTTP_SESSION_ATTR_USER_PREFERENCES, userPreferences);
                                session.setAttribute(HTTP_SESSION_ATTR_DATASETS, userDatasets);
                                session.setAttribute(HTTP_SESSION_ATTR_USERNAME, requestWrapper.getRemoteUser());
                            }

                            break;
                        }
                    }

                } catch (JAXBException|MessageException|ServiceException|JMSException e) {
                    LOGGER.error("Unable to retrieve userContext for username:{} and application:{}", requestWrapper.getRemoteUser(), getApplicationName(request.getServletContext()));
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

    private boolean isSessionInvalid(HttpSession session, String username, String currentRole, String currentScope) {
        if (!isEqualToSessionAttr(session, HTTP_SESSION_ATTR_ROLE_NAME, currentRole)
            || !isEqualToSessionAttr(session, HTTP_SESSION_ATTR_SCOPE_NAME, currentScope)
            || !isEqualToSessionAttr(session, HTTP_SESSION_ATTR_USERNAME, username)) {
            return true;
        }
        return false;
    }

    protected Map<String, List<Dataset>> getCategorizedDatasets(Context usmCtx) {
        Map<String, List<Dataset>> datasets = new LinkedHashMap<>();

        if (usmCtx != null && usmCtx.getScope() != null) {
            List<Dataset> datasetList = usmCtx.getScope().getDataset();

            for(Dataset dataset: datasetList) {
                List<Dataset> categoryDatasets = datasets.get(dataset.getCategory());

                if (categoryDatasets != null) {
                } else {
                    categoryDatasets = new LinkedList<>();
                }

                categoryDatasets.add(dataset);
                datasets.put(dataset.getCategory(), categoryDatasets);
            }

        }
        return datasets;
    }

    private Map<String, String> getUserPreferences(Context usmCtx, String applicationName) {
        Map<String, String> userPreferences = new HashMap<>();

        if (usmCtx.getPreferences() != null) {
            List<Preference> listPrefs = usmCtx.getPreferences().getPreference();
            //lets filter out all other preferences which comes from the other apps
            for (Preference pref : listPrefs) {
                if (pref.getApplicationName().equals(applicationName)) {
                    userPreferences.put(pref.getOptionName(), pref.getOptionValue());
                }
            }
        }

        return userPreferences;
    }

    private Set<String> getFeaturesAsString(Context usmCtx, String appName) {
        List<Feature> features = usmCtx.getRole().getFeature();
        Set<String> featuresStr = new HashSet<>(features.size());

        //extract only the features that the particular application is interested in
        for(Feature feature: features) {
            featuresStr.add(feature.getName());
        }
        return featuresStr;
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
