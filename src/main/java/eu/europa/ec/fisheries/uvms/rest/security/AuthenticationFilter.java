package eu.europa.ec.fisheries.uvms.rest.security;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.europa.ec.mare.usm.jwt.JwtTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters incoming requests, converting JWT token to a remote user identity (if
 * the request does not already reference a remote user), extending the duration
 * of the JWT token (if present).
 */
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String CHALLENGEAUTH = "/challengeauth";
    private static final String AUTHENTICATE = "/authenticate";
    private static final String PING = "/ping";

    @EJB
    private JwtTokenHandler tokenHandler;

    /**
     * Creates a new instance
     */
    public AuthenticationFilter() {
        super();
    }

    /**
     * Filters an incoming request, converting a (custom) JWT token to a (standard)
     * remote user identity (if the request does not already reference a remote
     * user), extending the duration of the JWT token (if present). If the request
     * contains neither a remote user identity nor a JWT token, request processing
     * is skipped and an HTTP status of 403 (Forbidden) is sent back to the
     * requester,
     *
     * @param request  The request we are processing
     * @param response The response we are creating
     * @param chain    The filter chain we are processing
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if another error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        LOGGER.debug("doFilter(" + httpRequest.getMethod() + ", " + httpRequest.getPathInfo() + ") - (ENTER)");
        Boolean tokenIsUsed = false;
        String remoteUser = httpRequest.getRemoteUser();
        String jwtToken = httpRequest.getHeader(AuthConstants.HTTP_HEADER_AUTHORIZATION);
        LOGGER.debug("httpRequest.getRemoteUser(): " + remoteUser);
        if (remoteUser == null) {
            tokenIsUsed = true;
            // decode token
            remoteUser = tokenHandler.parseToken(jwtToken);
        }
        LOGGER.debug("remoteUser: " + remoteUser);
        // check whether is an authenticated request or not
        if (remoteUser != null) {
            UserRoleRequestWrapper arequest = new UserRoleRequestWrapper(httpRequest, remoteUser);
            String refreshedToken;
            if (tokenIsUsed) {
                refreshedToken = tokenHandler.extendToken(jwtToken);
            } else {
                // we have a remote user but no token was provided
                refreshedToken = tokenHandler.createToken(remoteUser);
            }
            httpResponse.addHeader(AuthConstants.HTTP_HEADER_AUTHORIZATION, refreshedToken);

            if (PING.equals(httpRequest.getPathInfo())) {
                if (httpRequest.getUserPrincipal() != null
                        && httpRequest.getUserPrincipal().getClass().toString().contains("cas")) {
                    LOGGER.debug("ECAS Authenticated");
                    String callback = httpRequest.getParameter(AuthConstants.JWTCALLBACK);
                    if (callback != null) {
                        LOGGER.debug("Redirecting to add jwt");
                        String redir = callback + "?jwt=" + refreshedToken;
                        httpResponse.sendRedirect(redir);
                    }
                }
            }
            chain.doFilter(arequest, httpResponse);
        } else {
            String pathInfo = httpRequest.getPathInfo();

            if (AUTHENTICATE.equals(pathInfo) || CHALLENGEAUTH.equals(pathInfo)) {
                // if there is an authentication request proceed
                chain.doFilter(httpRequest, response);
            } else {
                // Send 403 error
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }
}
