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
}
