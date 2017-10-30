/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.rest.security; 

import java.io.IOException;

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

import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
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
            UserContext ctx = usmService.getFullUserContext(servletRequest.getRemoteUser(), getApplicationName(servletContext));

            if (ctx != null && ctx.getContextSet() != null) {
                if (!hasFeature(ctx, feature, null, null)) {
                    sendAccessForbidden(requestContext);
                }
            } else {
                sendAccessForbidden(requestContext);
            }
        } catch (ServiceException e) {
            sendAccessForbidden(requestContext);
        }

    }

    private void sendAccessForbidden(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .entity("User cannot access the resource.")
                .build());
    }

    private boolean hasFeature(UserContext userContext, UnionVMSFeature feature, String roleName, String scopeName) {
        if (servletRequest.getRemoteUser() == null) {
            return false;
        }

        for (eu.europa.ec.fisheries.wsdl.user.types.Context c : userContext.getContextSet().getContexts()) {
            for (Feature f : c.getRole().getFeature()) {
                if (feature.name().equals(f.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

}