package eu.europa.ec.fisheries.uvms.rest.security;

import eu.europa.ec.mare.usm.information.domain.UserContext;
import eu.europa.ec.mare.usm.information.domain.UserContextQuery;
import eu.europa.ec.mare.usm.information.service.InformationService;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;

/**
 * Created by georgige on 9/22/2015.
 */
public abstract class AbstractUSMHandler {

    private static String UNION_VMS_APPLICATION = "Union-VMS";

    public static String HTTP_HEADER_ROLE_NAME = "roleName";

    public static String HTTP_HEADER_SCOPE_NAME = "scopeName";

    public static String HTTP_SESSION_ATTR_ROLE_NAME = HTTP_HEADER_ROLE_NAME;

    public static String HTTP_SESSION_ATTR_SCOPE_NAME = HTTP_HEADER_SCOPE_NAME;

    public static String HTTP_SESSION_ATTR_ROLES_NAME = "listOfFeatures";


    /**
     * FIXME the service might not be running locally (it might be running within a different physical machine)
     * The following injection needs to be  changed into configurable lookup
     * (most probably, configured by the InitialContext of the hosting application)
     */
    @EJB
    private InformationService infoService;


    protected String getApplicationName(ServletContext servletContext) {
        String cfgName = servletContext.getInitParameter("usmApplication");
        if (cfgName == null) {
            cfgName = UNION_VMS_APPLICATION;
        }

       /*   see my comments in UnionVMSModule Enum
       UnionVMSModule application = UnionVMSModule.valueOf(cfgName);
        if (application == null) {
            return UNION_VMS_APPLICATION;
        }*/

        return cfgName;
    }

    protected UserContext getUserContext(String username, String applicationName) {
        UserContextQuery query = new UserContextQuery();
        query.setApplicationName(applicationName);
        query.setUserName(username);
        return getInfoService().getUserContext(query);
    }


    public InformationService getInfoService() {
        return infoService;
    }

    public void setInfoService(InformationService infoService) {
        this.infoService = infoService;
    }


}
