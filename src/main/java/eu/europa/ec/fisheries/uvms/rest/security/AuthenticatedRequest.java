package eu.europa.ec.fisheries.uvms.rest.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 */
public class AuthenticatedRequest extends HttpServletRequestWrapper {
  private final String remoteUser;
  
  public AuthenticatedRequest(HttpServletRequest delegate, 
                              String remoteUser) 
  {
    super(delegate);
    this.remoteUser = remoteUser;
  }

  @Override
  public String getRemoteUser() 
  {
    String ret;
    
    if (remoteUser != null) {
      ret = remoteUser;
    } else {
      ret = super.getRemoteUser(); 
    }
    
    return ret;
  }

}
