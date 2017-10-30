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

import java.security.Principal;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;

/**
 * An extension for the HTTPServletRequest that overrides the getUserPrincipal() and isUserInRole().
 *  We supply these implementations here, where they are not normally populated unless we are going through
 *  the facility provided by the container.
 * <p>If he user or roles are null on this wrapper, the parent request is consulted to try to fetch what ever the container has set for us.
 * This is intended to be created and used by the UserRoleFilter.
 * Created by georgige on 9/22/2015.
 *
 */
public class UserRoleRequestWrapper extends HttpServletRequestWrapper {


    private String user;

    private Set<String> roles = null;
    private HttpServletRequest realRequest;

    /**
     * a constructor which allows us to insert the available roles into the current request
     * @param user
     * @param roles
     * @param request
     */
    public UserRoleRequestWrapper(String user, Set<String> roles, HttpServletRequest request) {
        super(request);
        this.user = user;
        this.setRoles(roles);
        request.getServletContext().setAttribute(AuthConstants.HTTP_SERVLET_CONTEXT_ATTR_FEATURES, roles); //this is needed, because RESTEasy creates proxy objects and in a rest method, I cannot cast to this wrapper and call wrapper.getRoles();
        this.realRequest = request;
    }

    /**
     * sets only the username
     * @param delegate
     * @param remoteUser
     */
    public UserRoleRequestWrapper(HttpServletRequest delegate,
                                String remoteUser)
    {
        super(delegate);
        this.user = remoteUser;
    }

    @Override
    public String getRemoteUser()
    {
        String ret;

        if (user != null) {
            ret = user;
        } else {
            ret = super.getRemoteUser();
        }

        return ret;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (getRoles() == null) {
            return this.realRequest.isUserInRole(role);
        }
        return getRoles().contains(role);
    }

    @Override
    public Principal getUserPrincipal() {
        if (this.user == null) {
            return realRequest.getUserPrincipal();
        }

        // make an anonymous implementation to just return our user
        return new Principal() {
            @Override
            public String getName() {
                return user;
            }
        };
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

}