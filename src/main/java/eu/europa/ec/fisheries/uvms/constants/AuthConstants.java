package eu.europa.ec.fisheries.uvms.constants;

import javax.ws.rs.core.HttpHeaders;

/**
 * Created by georgige on 10/2/2015.
 */
public interface AuthConstants {
    public static final String HTTP_HEADER_ROLE_NAME = "roleName";

    String HTTP_HEADER_SCOPE_NAME = "scopeName";

    String HTTP_HEADER_AUTHORIZATION = HttpHeaders.AUTHORIZATION;
    String JWTCALLBACK = "jwtcallback";

    String HTTP_SERVLET_CONTEXT_ATTR_FEATURES = "servletContextUserFeatures";

    String HTTP_SESSION_ATTR_ROLES_NAME = HTTP_SERVLET_CONTEXT_ATTR_FEATURES;

    String CACHE_NAME_USER_SESSION = "userSessionCache";
    String CACHE_NAME_APP_MODULE = "appModuleCache";
}
