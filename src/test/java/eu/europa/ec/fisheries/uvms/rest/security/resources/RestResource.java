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
package eu.europa.ec.fisheries.uvms.rest.security.resources;

import java.util.Date;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import eu.europa.ec.fisheries.wsdl.user.types.Application;

@Path("/rest")
public class RestResource {

	final static Logger LOG = LoggerFactory.getLogger(RestResource.class);
	private static final String TEST_USER_PREFERENCE = "TEST_USER_PREFERENCE";
	public static final String SOME_TEST_OPTION = "someTestOption";
	public static final String DATASET_CATEGORY = "restrictionAreas";
	private static final String APP_NAME = "Reporting";

	@Context
	private UriInfo context;

	@EJB
	private USMService usmService;

	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listReports(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		if (!request.isUserInRole("LIST_REPORTS")) {
			return Response.status(HttpServletResponse.SC_FORBIDDEN).build();
		}

		return Response.status(HttpServletResponse.SC_OK).build();
	}

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReport(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		if (!request.isUserInRole("NON_EXISTING_TEST_FEATURE")) {
			return Response.status(HttpServletResponse.SC_FORBIDDEN).build();
		}

		return Response.status(HttpServletResponse.SC_OK).build();
	}

	@GET
	@Path("/applicationDescriptor")
	@Produces(MediaType.APPLICATION_JSON)
	public Response applicationDescriptor(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		long timeDiff = new Date().getTime();

		Application application = null;
		try {
			application = usmService.getApplicationDefinition(APP_NAME);
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		timeDiff = new Date().getTime() - timeDiff;

		if (application == null || !APP_NAME.equals(application.getName())) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(String.valueOf(timeDiff))
					.build();
		}

		try {
			application = usmService.getApplicationDefinition(APP_NAME);
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		if (application == null || !APP_NAME.equals(application.getName())) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(String.valueOf(timeDiff))
					.build();
		}

		return Response.status(HttpServletResponse.SC_OK).entity(String.valueOf(timeDiff)).build();
	}

	@GET
	@Path("/options")
	@Produces(MediaType.APPLICATION_JSON)
	public Response options(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		String newDefaultValue = "newValue" + new Date().getTime();

		try {
			usmService.setOptionDefaultValue(TEST_USER_PREFERENCE, newDefaultValue, APP_NAME);
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		String optionDefaultValue;
		long timeDiff = new Date().getTime();
		try {
			optionDefaultValue = usmService.getOptionDefaultValue(TEST_USER_PREFERENCE, APP_NAME);
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}
		timeDiff = new Date().getTime() - timeDiff;

		if (optionDefaultValue == null || !newDefaultValue.equals(optionDefaultValue)) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		timeDiff = new Date().getTime() - timeDiff;

		long timeDiff2 = new Date().getTime();
		try {
			optionDefaultValue = usmService.getOptionDefaultValue(TEST_USER_PREFERENCE, APP_NAME);
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}
		timeDiff2 = new Date().getTime() - timeDiff2;

		if (optionDefaultValue == null || !newDefaultValue.equals(optionDefaultValue) || timeDiff2 >= timeDiff) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		return Response.status(HttpServletResponse.SC_OK).build();
	}

	@GET
	@Path("/preferences")
	@Produces(MediaType.APPLICATION_JSON)
	public Response preferences(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		String newValue = "valueeeee" + new Date().getTime();
		String cacheKey = "someCacheKeydratgv asdf aert34sdfgds";

		try {
			usmService.putUserPreference(TEST_USER_PREFERENCE, newValue, APP_NAME, "EC", "rep_power_role", "rep_power");
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		eu.europa.ec.fisheries.wsdl.user.types.Context ctxt = null;
		try {
			ctxt = usmService.getUserContext("rep_power", APP_NAME, "rep_power_role", "EC");
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		String userPref = null;
		try {
			userPref = usmService.getUserPreference(TEST_USER_PREFERENCE, ctxt);
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		if (!newValue.equals(userPref)) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		try {
			userPref = usmService.getUserPreference(TEST_USER_PREFERENCE, ctxt);
		} catch (ServiceException e) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		if (!newValue.equals(userPref)) {
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}

		return Response.status(HttpServletResponse.SC_OK).build();
	}
}