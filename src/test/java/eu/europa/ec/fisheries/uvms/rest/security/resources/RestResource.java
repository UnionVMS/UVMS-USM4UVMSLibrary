package eu.europa.ec.fisheries.uvms.rest.security.resources;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rest")
public class RestResource {

    final static Logger LOG = LoggerFactory.getLogger(RestResource.class);
    
    @Context
	private UriInfo context;
	

    @GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
    public Response listReports(@Context HttpServletRequest request,
    				@Context HttpServletResponse response) {

        if (!request.isUserInRole("APP1_TEST_FEATURE")) {
            return Response.status(HttpServletResponse.SC_FORBIDDEN).build();
        }

    	return Response.status(HttpServletResponse.SC_OK).build();
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReport(@Context HttpServletRequest request,
                                @Context HttpServletResponse response) {

        if (!request.isUserInRole("NON_EXISTING_TEST_FEATURE")) {
            return Response.status(HttpServletResponse.SC_FORBIDDEN).build();
        }

        return Response.status(HttpServletResponse.SC_OK).build();
    }

}
