package eu.europa.ec.fisheries.uvms.init;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import static java.lang.Integer.valueOf;
//FIXME
@Ignore
public class ModuleInitializerBeanTest {

    public static final int PORT = 8089;
    public static final String LOCALHOST = "localhost:" + valueOf(PORT);
    public static final String KEEP_ALIVE = "Keep-Alive";
    public static final String APPLICATION_XML = "application/xml";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final int NOT_PRESENT = HttpServletResponse.SC_NO_CONTENT;
    public static final int PRESENT = HttpServletResponse.SC_OK;
    public static final int SUCCESS = HttpServletResponse.SC_OK;

    private static final String JWTOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJ1c20vYXV0aGVudGljYXRpb24iLCJpc3MiOiJ1c20iLCJzdWIiOiJhdXRoZW50aWNhdGlvbiIsImlhdCI6MTQzNzY1NDMxNSwiZXhwIjoxNDM3NjU2MTE1LCJ1c2VyTmFtZSI6InVzbV9hZG1pbiJ9.TqR4eRZnToXPCswFgUzzw8hBXBO7fRdi1oDyLTRjpaA";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    private ModuleInitializerBean initializerBean;

    @Before
    public void setUp() throws Exception {
        initializerBean = new ModuleInitializerBean();
    }

    @Test
    public void shouldDeployDescriptor() throws Exception {
        // given
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/usm-administration/rest/authenticate"))
                .willReturn(WireMock.aResponse()
                        .withStatus(SUCCESS)
                        .withHeader(CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON_TYPE))
                        .withBody("{" +
                                " \"jwtoken\": \"" + JWTOKEN + "\"," +
                                " \"authenticated\": true,\n" +
                                " \"statusCode\": 0\n" +
                                "}")));
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/usm-administration/rest/deployments/reportingModule"))
                .willReturn(WireMock.aResponse()
                        .withStatus(NOT_PRESENT)));

        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/usm-administration/rest/deployments/"))
                .willReturn(WireMock.aResponse()
                        .withStatus(SUCCESS)
                        .withHeader(CONTENT_TYPE, APPLICATION_XML)
                        .withBody("<response>OK</response>")));

        // when
       // initializerBean.startup();

        // then
        WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/usm-administration/rest/authenticate"))
                        .withHeader(CONTENT_TYPE, WireMock.matching(String.valueOf(MediaType.APPLICATION_JSON_TYPE)))
                        .withHeader("Host", WireMock.matching(LOCALHOST))
                        .withRequestBody(WireMock.equalToJson( "{\"userName\":\"usm_bootstrap\",\"password\":\"password\"}"))
        );

        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/usm-administration/rest/deployments/reportingModule"))
                        .withHeader("Accept", WireMock.matching(APPLICATION_XML))
                        .withHeader("Host", WireMock.matching(LOCALHOST))
                        .withHeader("Connection", WireMock.matching(KEEP_ALIVE))
                        .withHeader(AuthConstants.HTTP_HEADER_AUTHORIZATION, WireMock.matching(JWTOKEN))
                        .withoutHeader(CONTENT_TYPE)
        );

        WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/usm-administration/rest/deployments/"))
                        .withHeader("Accept", WireMock.matching(APPLICATION_XML))
                        .withHeader("Host", WireMock.matching(LOCALHOST))
                        .withHeader("Connection", WireMock.matching(KEEP_ALIVE))
                        .withHeader(AuthConstants.HTTP_HEADER_AUTHORIZATION, WireMock.matching(JWTOKEN))
                        .withRequestBody(WireMock.matching(".*<name>reporting</name>.*"))
        );
    }

    @Test
    public void shouldUpdateDescriptor() throws Exception {
        // given
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/usm-administration/rest/authenticate"))
                .willReturn(WireMock.aResponse()
                        .withStatus(SUCCESS)
                        .withHeader(CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON_TYPE))
                        .withBody("{" +
                                " \"jwtoken\": \"" + JWTOKEN + "\"," +
                                " \"authenticated\": true,\n" +
                                " \"statusCode\": 0\n" +
                                "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/usm-administration/rest/deployments/reportingModule"))
                .willReturn(WireMock.aResponse()
                        .withStatus(PRESENT)
                        .withHeader(CONTENT_TYPE, APPLICATION_XML)
                        .withBody("<response>OK</response>")));

        WireMock.stubFor(WireMock.put(WireMock.urlEqualTo("/usm-administration/rest/deployments/"))
                .willReturn(WireMock.aResponse()
                        .withStatus(SUCCESS)
                        .withHeader(CONTENT_TYPE, APPLICATION_XML)
                        .withBody("<response>OK</response>")));

        // when
        //initializerBean.startup();

        // then
        WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/usm-administration/rest/authenticate"))
                        .withHeader(CONTENT_TYPE, WireMock.matching(String.valueOf(MediaType.APPLICATION_JSON_TYPE)))
                        .withHeader("Host", WireMock.matching(LOCALHOST))
                        .withRequestBody(WireMock.equalToJson( "{\"userName\":\"usm_bootstrap\",\"password\":\"password\"}"))
        );

        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/usm-administration/rest/deployments/reportingModule"))
                        .withHeader("Accept", WireMock.matching("application/xml"))
                        .withHeader("Host", WireMock.matching(LOCALHOST))
                        .withHeader("Connection", WireMock.matching(KEEP_ALIVE))
                        .withHeader(AuthConstants.HTTP_HEADER_AUTHORIZATION, WireMock.matching(JWTOKEN))
                        .withoutHeader(CONTENT_TYPE)
        );

        WireMock.verify(1, WireMock.putRequestedFor(WireMock.urlEqualTo("/usm-administration/rest/deployments/"))
                        .withHeader("Accept", WireMock.matching(APPLICATION_XML))
                        .withHeader("Content-Type", WireMock.matching(APPLICATION_XML))
                        .withHeader("Host", WireMock.matching(LOCALHOST))
                        .withHeader("Connection", WireMock.matching(KEEP_ALIVE))
                        .withHeader("Content-Length", WireMock.matching("866"))
                        .withHeader(AuthConstants.HTTP_HEADER_AUTHORIZATION, WireMock.matching(JWTOKEN))
                        .withRequestBody(WireMock.matching(".*<name>reporting</name>.*"))
        );

    }

}