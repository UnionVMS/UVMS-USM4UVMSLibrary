package eu.europa.ec.fisheries.uvms.constants;

import javax.ws.rs.core.HttpHeaders;

/**
 * Created by georgige on 10/2/2015.
 */
public interface AuthConstants {
    public static final String HTTP_HEADER_ROLE_NAME = "roleName";

    public static final String HTTP_HEADER_SCOPE_NAME = "scopeName";

    public static final String HTTP_HEADER_AUTHORIZATION = HttpHeaders.AUTHORIZATION;
    public static final String JWTCALLBACK = "jwtcallback";

    public static final String HTTP_SERVLET_CONTEXT_ATTR_FEATURES = "servletContextUserFeatures";

    public static String HTTP_SESSION_ATTR_ROLE_NAME = HTTP_HEADER_ROLE_NAME;

    public static String HTTP_SESSION_ATTR_SCOPE_NAME = HTTP_HEADER_SCOPE_NAME;

    public static String HTTP_SESSION_ATTR_ROLES_NAME = HTTP_SERVLET_CONTEXT_ATTR_FEATURES;

    public static String HTTP_SESSION_ATTR_DATASETS = "scopeCategorizedDatasets";

    public static String HTTP_SESSION_ATTR_USER_PREFERENCES = "userPreferencesMap";
}
