package eu.europa.ec.fisheries.uvms.rest.security; 

import java.io.IOException;

import javax.jms.JMSException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.wsdl.user.types.Feature;
import eu.europa.ec.fisheries.wsdl.user.types.UserContext;

@Provider
public class UnionVMSFeatureFilter extends AbstractUSMHandler implements ContainerRequestFilter {

    @Context
    ResourceInfo resourceInfo;

    @Context
    private ServletContext servletContext;

    @Context
    private HttpServletRequest servletRequest;

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

        try {
            UserContext ctx = getUserContext(servletRequest.getRemoteUser(), getApplicationName(servletContext));

            if (ctx != null && ctx.getContextSet() != null) {
                if (!hasFeature(ctx, feature, null, null)) {
                    sendAccessForbidden(requestContext);
                }
            } else {
                sendAccessForbidden(requestContext);
            }
        } catch (JAXBException|MessageException|ServiceException|JMSException e) {
            sendAccessForbidden(requestContext);
        }

    }

    private void sendAccessForbidden(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .entity("User cannot access the resource.")
                .build());
    }

    private boolean filterRole(eu.europa.ec.fisheries.wsdl.user.types.Context ctx, String role) {
        if (ctx.getRole() == null) {
            return false;
        }

        return role == null || role.equals(ctx.getRole().getRoleName()); 
    }

    private boolean filterScope(eu.europa.ec.fisheries.wsdl.user.types.Context ctx, String scope) {
        return ctx.getScope() == null || ctx.getScope().getScopeName().equals(scope);
    }

    private boolean hasFeature(UserContext userContext, UnionVMSFeature feature, String roleName, String scopeName) {
        if (servletRequest.getRemoteUser() == null) {
            return false;
        }

        for (eu.europa.ec.fisheries.wsdl.user.types.Context c : userContext.getContextSet().getContexts()) {
            if (!filterRole(c, roleName) || !filterScope(c, scopeName)) {
                continue;
            }

            for (Feature f : c.getRole().getFeature()) {
                if (feature.name().equals(f.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

}
