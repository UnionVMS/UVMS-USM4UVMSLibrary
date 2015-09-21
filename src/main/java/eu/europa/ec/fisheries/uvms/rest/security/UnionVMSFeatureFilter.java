package eu.europa.ec.fisheries.uvms.rest.security; 

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import eu.europa.ec.mare.usm.information.domain.Feature;
import eu.europa.ec.mare.usm.information.domain.UserContext;
import eu.europa.ec.mare.usm.information.domain.UserContextQuery;
import eu.europa.ec.mare.usm.information.service.InformationService;

@Provider
public class UnionVMSFeatureFilter implements ContainerRequestFilter {

	private static String UNION_VMS_APPLICATION = "Union-VMS";

    @Context
    ResourceInfo resourceInfo; 

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private ServletContext servletContext;

    @EJB
    private InformationService infoService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UnionVMSFeature feature;
        if (resourceInfo.getResourceMethod().isAnnotationPresent(RequiresFeature.class)) {
            feature = resourceInfo.getResourceMethod().getAnnotation(RequiresFeature.class).value();
        }
        else if (resourceInfo.getResourceClass().isAnnotationPresent(RequiresFeature.class)) {
            feature = resourceInfo.getResourceClass().getAnnotation(RequiresFeature.class).value();
        }
        else {
            return;
        }

        if (!hasFeature(servletRequest.getRemoteUser(), feature, null, null)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
            		.header("content-type", "text/plain")
            		.entity("User cannot access the resource.")
            		.build());
        }
    }

    private boolean filterRole(eu.europa.ec.mare.usm.information.domain.Context ctx, String role) {
        if (ctx.getRole() == null) {
            return false;
        }

        return role == null || role.equals(ctx.getRole().getRoleName()); 
    }

    private boolean filterScope(eu.europa.ec.mare.usm.information.domain.Context ctx, String scope) {
        return ctx.getScope() == null || ctx.getScope().getScopeName().equals(scope);
    }

    private boolean hasFeature(String userName, UnionVMSFeature feature, String roleName, String scopeName) {
        if (userName == null) {
            return false;
        }

		String applicationName = getApplicationName();

        UserContext ctx = getUserContext(userName, applicationName);
        if (ctx == null || ctx.getContextSet() == null) {
            return false;
        }

        for (eu.europa.ec.mare.usm.information.domain.Context c : ctx.getContextSet().getContexts()) {
            if (!filterRole(c, roleName) || !filterScope(c, scopeName)) {
                continue;
            }

            for (Feature f : c.getRole().getFeatures()) {
                if (applicationName.equals(f.getApplicationName()) && feature.name().equals(f.getFeatureName())) {
                    return true;
                }
            }
        }

        return false;
    }

	private String getApplicationName() {
		String cfgName = servletContext.getInitParameter("usmApplication");
		if (cfgName == null) {
			return UNION_VMS_APPLICATION;
		}

		UnionVMSModule application = UnionVMSModule.valueOf(cfgName);
		if (application == null) {
			return UNION_VMS_APPLICATION;
		}

		return application.name();
	}

    private UserContext getUserContext(String userName, String applicationName) {
        UserContextQuery query = new UserContextQuery();
        query.setApplicationName(applicationName);
        query.setUserName(userName);
        return infoService.getUserContext(query);
    }
}
