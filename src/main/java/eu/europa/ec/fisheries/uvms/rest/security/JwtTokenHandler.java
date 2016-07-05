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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the creation, extension (of validity) and verification (parsing) 
 * of JWT tokens.
 */
public class JwtTokenHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenHandler.class);
  private static final String PROPERTIES_FILE = "jwt.properties";
  private static final String PROP_KEY = "secretKey";
  private static final String PROP_SUBJECT = "subject";
  private static final String PROP_ISSUER = "issuer";
  private static final String PROP_ID = "id";
  private static final long DEFAULT_TTL = (30 * 60 * 1000);
  private static final String DEFAULT_KEY = "";
  private static final String DEFAULT_ID = "";
  private static final String DEFAULT_ISSUER = "";
  private static final String DEFAULT_SUBJECT = "";

  private static final String USER_NAME = "userName";

  private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
  private Properties properties = new Properties();

  /**
   * Creates a new instance
   */
  public JwtTokenHandler() 
  {
    InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
    if (is != null) {
      try {
        properties.load(is);
      } catch (IOException e) {
        LOGGER.warn("Failed to load class-path resource:'" + 
                    PROPERTIES_FILE + "'. Using default values", e);
      }
    } else {
      LOGGER.info("Class-path resource: '" + PROPERTIES_FILE + 
                  "' does not exist. Using default values");
    }
  }

  /**
   * Creates a JWT token identifying the user with the provided name.
   * 
   * @param userName the user name.
   * 
   * @return a JWT token identifying the user, or null if the provided was 
   * null or empty
   */
  public String createToken(String userName) 
  {
    LOGGER.info("createToken(" +  userName + ") - (ENTER)");
    
    String ret = null;
    
    if (userName != null && !userName.trim().isEmpty()) {
      long now = System.currentTimeMillis();

      // Claims
      Claims claims = Jwts.claims();
      claims.setId(properties.getProperty(PROP_ID, DEFAULT_ID));
      claims.setIssuer(properties.getProperty(PROP_ISSUER, DEFAULT_ISSUER));
      claims.setSubject(properties.getProperty(PROP_SUBJECT, DEFAULT_SUBJECT));
      claims.setIssuedAt(new Date(now));
      claims.setExpiration(new Date(now + DEFAULT_TTL));
      claims.put(USER_NAME, userName);

      ret = signClaims(claims);
    }
    
    LOGGER.info("createToken() - (LEAVE)");
    return ret;
  }

  /**
   * Extends the validity period of the provided token.
   * 
   * @param token the JWT token to be extended
   * 
   * @return an extended (validity) version of the token, or null if the 
   * provided input was invalid or already expired.
   */
  public String extendToken(String token) 
  {
    LOGGER.info("extendToken(" + token + ") - (ENTER)");
    
    String ret = null;
    Claims claims = parseClaims(token);
    if (claims != null) {

      long now = System.currentTimeMillis();
      claims.setIssuedAt(new Date(now));
      claims.setExpiration(new Date(now + DEFAULT_TTL));

      ret = signClaims(claims);
    }
    
    LOGGER.info("extendToken() - (LEAVE)");
    return ret;
  }

  /**
   * Extract the name of the user to which the provided token was issued.
   * 
   * @param token the JWT token to be parsed
   * 
   * @return the name of the user, or null if the provided input was invalid 
   * or expired.
   */
  public String parseToken(String token) 
  {
    LOGGER.info("parseToken(" + token + ") - (ENTER)");
    
    String ret = null;

    Claims claims = parseClaims(token);
    if (claims != null) {
      ret = (String) claims.get(USER_NAME);
    }

    LOGGER.info("parseToken() - (LEAVE)");
    return ret;
  }

  private String signClaims(Claims claims) 
  {
    // Header
    Map<String, Object> header = new HashMap<>();
    header.put(Header.TYPE, Header.JWT_TYPE);
    header.put(JwsHeader.ALGORITHM, signatureAlgorithm);
    
    // Signature key
    Key key = new SecretKeySpec(getSecretKey(), 
                                signatureAlgorithm.getJcaName());

    String ret = Jwts.builder().
            setHeader(header).
            setClaims(claims).
            signWith(signatureAlgorithm, key).
            compact();
  
    return ret;
  }

  private Claims parseClaims(String token) 
  {
    Claims ret = null;

    if (token != null && !token.trim().isEmpty()) {
      try {
        ret = Jwts.parser().
                   setSigningKey(getSecretKey()).
                   parseClaimsJws(token).getBody();
      } catch (ExpiredJwtException e) {
        LOGGER.error("Token expired");
      } catch (UnsupportedJwtException |
              MalformedJwtException | SignatureException |
              IllegalArgumentException e) {
        LOGGER.error("Failed to parse token");
      }
    }

    return ret;
  }

  private byte[] getSecretKey() 
  {
    String secretKey = properties.getProperty(PROP_KEY, DEFAULT_KEY);

    return DatatypeConverter.parseBase64Binary(secretKey);
  }
}