package eu.europa.ec.fisheries.uvms.security.rest.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import eu.europa.ec.fisheries.uvms.init.ModuleInitializerBean;
import eu.europa.ec.fisheries.uvms.rest.security.AuthorizationFilter;
import eu.europa.ec.fisheries.uvms.rest.security.JwtTokenHandler;
import eu.europa.ec.fisheries.uvms.security.rest.util.ArquillianTest;
import eu.europa.ec.mare.usm.information.domain.*;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.extension.rest.client.Header;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@RunAsClient
public class RestResourceITest extends ArquillianTest {
	
    @ArquillianResource
    URL contextPath;


	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testAuthorizationPositive(@ArquillianResteasyResource("test/rest") ResteasyWebTarget webTarget) throws JsonParseException, JsonMappingException, IOException {
		
		//check if we have the prerequisite - a report in the DB with ID = 1
		Response response = webTarget.path("/list" ).request()
				.header(HttpHeaders.AUTHORIZATION, new JwtTokenHandler().createToken("TEST_USER"))
				.header(AuthorizationFilter.HTTP_HEADER_ROLE_NAME, "TEST_ROLE")
				.header(AuthorizationFilter.HTTP_HEADER_SCOPE_NAME, "TEST_SCOPE")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testAuthorizationNegative(@ArquillianResteasyResource("test/rest") ResteasyWebTarget webTarget) throws JsonParseException, JsonMappingException, IOException {

		//check if we have the prerequisite - a report in the DB with ID = 1
		Response response = webTarget.path("/get" ).request()
				.header(HttpHeaders.AUTHORIZATION, new JwtTokenHandler().createToken("TEST_USER"))
				.header(AuthorizationFilter.HTTP_HEADER_ROLE_NAME, "TEST_ROLE")
				.header(AuthorizationFilter.HTTP_HEADER_SCOPE_NAME, "TEST_SCOPE")
				.get();
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		response.close();
	}

/*
//TODO not implemented yet since USM guys will implement new REST methods, dedicated to user preferences
	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testUserPreferencesDefaultValue(@ArquillianResteasyResource("test/rest") ResteasyWebTarget webTarget) throws JsonParseException, JsonMappingException, IOException {

		//check if we have the prerequisite - a report in the DB with ID = 1
		Response response = webTarget.path("/get" ).request()
				.header(HttpHeaders.AUTHORIZATION, new JwtTokenHandler().createToken("TEST_USER"))
				.header(AuthorizationFilter.HTTP_HEADER_ROLE_NAME, "TEST_ROLE")
				.header(AuthorizationFilter.HTTP_HEADER_SCOPE_NAME, "TEST_SCOPE")
				.get();
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		response.close();
	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testUserPreferencesCustomValue(@ArquillianResteasyResource("usm-information/rest") ResteasyWebTarget usmWebTarget, @ArquillianResteasyResource("test/rest") ResteasyWebTarget webTarget) throws JsonParseException, JsonMappingException, IOException {
		//TODO add new preference
		usmWebTarget.path("/userContext").request(MediaType.APPLICATION_JSON_TYPE)
				.header(HttpHeaders.AUTHORIZATION, new JwtTokenHandler().createToken("TEST_USER"))
				.put(Entity.json(new UserContext().))

		//TODO call your test rest which returns the value of the preference
		Response response = webTarget.path("/get" ).request()
				.header(HttpHeaders.AUTHORIZATION, new JwtTokenHandler().createToken("TEST_USER"))
				.header(AuthorizationFilter.HTTP_HEADER_ROLE_NAME, "TEST_ROLE")
				.header(AuthorizationFilter.HTTP_HEADER_SCOPE_NAME, "TEST_SCOPE")
				.get();
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		response.close();
	}
*/

}
