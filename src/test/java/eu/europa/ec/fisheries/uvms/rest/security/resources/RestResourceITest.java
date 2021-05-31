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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.extension.rest.client.Header;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.rest.security.util.ArquillianTest;
import eu.europa.ec.mare.usm.jwt.DefaultJwtTokenHandler;

@RunWith(Arquillian.class)
@RunAsClient
public class RestResourceITest extends ArquillianTest {
	
	@Before
	public void setUp() throws Exception {
		/*UserContext newUserCtx = new UserContext();

		Feature newFeature = new Feature();
		newFeature.setApplicationName("TEST_APP");
		newFeature.setFeatureName("APP1_TEST_FEATURE");
		Set<Feature> features = new HashSet<>();
		features.add(newFeature);

		Role newRole = new Role();
		newRole.setRoleName("TEST_ROLE");
		newRole.setFeatures(features);

		Preferences newPrefs = new Preferences();
//		Preference pref = new Preference();
//		pref.setApplicationName(newFeature.getApplicationName());
//		pref.setOptionName("TEST_USER_PREFERENCE");
//		pref.setOptionValue("CUSTOM_USER_VALUE");
		Set<Preference> prefSet = new HashSet<>();
//		prefSet.add(pref);
		newPrefs.setPreferences(prefSet);

		Scope newScope = new Scope();
		Calendar todayCal = Calendar.getInstance();
		newScope.setActiveFrom(todayCal.getTime());
		todayCal.add(Calendar.YEAR, 3);
		newScope.setActiveTo(todayCal.getTime());
		newScope.setScopeName("TEST_SCOPE");

		Context newCtx = new Context();
		newCtx.setRole(newRole);
		newCtx.setPreferences(newPrefs);
		newCtx.setScope(newScope);

		ContextSet ctxSet = new ContextSet();
		Set<Context> ctxs = new HashSet<>();
		ctxs.add(newCtx);
		ctxSet.setContexts(ctxs);

		newUserCtx.setApplicationName(newFeature.getApplicationName());
		newUserCtx.setContextSet(ctxSet);
		newUserCtx.setUserName("TEST_USER");
//		InputStream in = getClass().getResourceAsStream("config.properties");
//		Properties props = new Properties();
//		props.load(in);

		ResteasyWebTarget usmWebTarget = new ResteasyClientBuilder().build().target("http://localhost:8080/usm-information/rest");
		Response response = usmWebTarget.path("/userContext").request(MediaType.APPLICATION_JSON_TYPE)
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("TEST_USER")).put(Entity.json(newUserCtx));

		assert response.getStatus() == HttpServletResponse.SC_OK;*/
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testAuthorizationPositive(@ArquillianResteasyResource("test/rest") ResteasyWebTarget webTarget) throws IOException {
		
		//check if we have the prerequisite - a report in the DB with ID = 1
		Response response = webTarget.path("/list" ).request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("rep_power"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "rep_power_role")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "EC")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testAuthorizationNegative(@ArquillianResteasyResource("test/rest") ResteasyWebTarget webTarget) throws IOException {

		//check if we have the prerequisite - a report in the DB with ID = 1
		Response response = webTarget.path("/get" ).request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("Some Fake User"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "TEST_ROLE")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "TEST_SCOPE")
				.get();
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		response.close();
	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testApplicationDescriptor(@ArquillianResteasyResource("test/rest/applicationDescriptor") ResteasyWebTarget webTarget) throws IOException, ServiceException {

		Response response = webTarget.request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("rep_power"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "rep_power_role")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "EC")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

//		RestResource res = new RestResource();
//		res.applicationDescriptor();
//		res.datasets();
//		res.features();
//		res.options();
//		res.preferences();
	}


	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testDatasetsMerge(@ArquillianResteasyResource("test/rest/datasets/merge") ResteasyWebTarget webTarget) throws IOException, ServiceException {

		Response response = webTarget.request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("rep_power"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "rep_power_role")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "EC")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testDatasetsReset(@ArquillianResteasyResource("test/rest/datasets/reset") ResteasyWebTarget webTarget) throws IOException, ServiceException {

		Response response = webTarget.request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("rep_power"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "rep_power_role")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "EC")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testFeatures(@ArquillianResteasyResource("test/rest/features") ResteasyWebTarget webTarget) throws IOException, ServiceException {

		Response response = webTarget.request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("rep_power"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "rep_power_role")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "EC")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

//		RestResource res = new RestResource();
//		res.options();
//		res.preferences();
	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testOptions(@ArquillianResteasyResource("test/rest/options") ResteasyWebTarget webTarget) throws IOException, ServiceException {

		Response response = webTarget.request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("rep_power"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "rep_power_role")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "EC")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

//		RestResource res = new RestResource();
//		res.preferences();
	}

	@Test
	@Header(name="connection", value = "Keep-Alive")
	public void testPreferences(@ArquillianResteasyResource("test/rest/preferences") ResteasyWebTarget webTarget) throws IOException, ServiceException {

		Response response = webTarget.request()
				.header(AuthConstants.HTTP_HEADER_AUTHORIZATION, new DefaultJwtTokenHandler().createToken("rep_power"))
				.header(AuthConstants.HTTP_HEADER_ROLE_NAME, "rep_power_role")
				.header(AuthConstants.HTTP_HEADER_SCOPE_NAME, "EC")
				.get();
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		response.close();

//		RestResource res = new RestResource();
//		res.preferences();
	}
}
