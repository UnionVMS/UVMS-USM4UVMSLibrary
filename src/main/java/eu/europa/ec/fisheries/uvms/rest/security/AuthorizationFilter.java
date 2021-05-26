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

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import eu.europa.ec.fisheries.wsdl.user.types.Context;
import java.io.IOException;
import java.util.Set;
import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by georgige on 9/22/2015.
 */
public class AuthorizationFilter extends AbstractUSMHandler implements Filter, AuthConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

    @EJB
    private USMService usmService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request instanceof UserRoleRequestWrapper) {
            UserRoleRequestWrapper requestWrapper = (UserRoleRequestWrapper) request;
            Set<String> featuresStr = null;
            String applicationName = getApplicationName(request.getServletContext());
            String currentScope = requestWrapper.getHeader(HTTP_HEADER_SCOPE_NAME); // get it from the header
            String currentRole = requestWrapper.getHeader(HTTP_HEADER_ROLE_NAME); // get it from the header
            LOGGER.debug("Current requests is with scope '{}', and role '{}'", currentScope, currentRole);
            try {
                Context userContext = usmService.getUserContext(requestWrapper.getRemoteUser(), applicationName, currentRole, currentScope);
                if (userContext == null) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    featuresStr = usmService.getUserFeatures(requestWrapper.getRemoteUser(), userContext);
                }
            } catch (ServiceException | IOException e) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unable to get user context and/or user features.");
                return;
            }
            if (featuresStr == null) {
                LOGGER.warn(
                        "Unauthorized attempt to access resource with scope '{}' and role '{}', which don't exist for the current user.",
                        currentScope, currentRole);
            } else {
                requestWrapper.setRoles(featuresStr);
            }
        }
        try {
            chain.doFilter(request, response);
        } catch (IOException e) {
            LOGGER.error("failed to call WebFilter chain.doFilter(request,response). Caused by: {}", e.getMessage());
        }
    }

    @Override
    public void destroy() {
    }
}