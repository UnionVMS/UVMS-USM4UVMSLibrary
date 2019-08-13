package eu.europa.ec.fisheries.uvms.rest.security;

import eu.europa.ec.mare.usm.jwt.JwtTokenHandler;

import javax.inject.Inject;
import java.util.List;

public class InternalRestTokenHandler {

    @Inject
    private JwtTokenHandler handler;

    private String token;

    private InternalRestTokenHandler(){}

    public String createAndFetchToken(String username, List<Integer> features) {
        if(token == null) {
            token = handler.createToken(username, features);
        }
        return token;
    }
}
