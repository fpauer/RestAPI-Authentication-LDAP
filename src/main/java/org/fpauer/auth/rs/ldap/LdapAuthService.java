/**
 * Copyright 2012 Fernando Pauer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fpauer.auth.rs.ldap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fpauer.auth.rs.AccessConfig;
import org.fpauer.auth.rs.AccessToken;
import org.fpauer.auth.rs.AuthException;
import org.fpauer.auth.rs.AuthResponse;
import org.fpauer.auth.rs.AuthService;
import org.fpauer.auth.rs.Configuration;
import org.fpauer.auth.rs.StringUtils;
import org.fpauer.auth.rs.ldap.LdapConfig.Keys;

import com.sun.jersey.core.util.Base64;

/**
 * @author Fernando Pauer
 *
 */
@Path("/auth")
public class LdapAuthService implements AuthService {
    private static Logger logger = Logger.getLogger(LdapAuthService.class.getName());

    @GET
    @Path("/ldap/{username}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse authenticateGet(@PathParam("username") String username, @PathParam("password") String password) {
        return authenticate(null, username, password);
    }

    @GET
    @Path("/ldap/{server}/{username}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse authenticateGet(@PathParam("server") String server, @PathParam("username") String username,
                                        @PathParam("password") String password) {
        return authenticate(server, username, password);
    }

    @GET
    @Path("/accessToken/{accessToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse authenticateTokenGet(@PathParam("accessToken") String accessToken) {
    	return authenticateTokenGet(null, accessToken);
    }
    
    @GET
    @Path("/accessToken/{server}/{accessToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse authenticateTokenGet(@PathParam("server") String server, @PathParam("accessToken") String accessToken) {

    	//max minutes allowed til an access token get expired
        int expired_minutes = StringUtils.toInt(Configuration.get(server, AccessConfig.Keys.ACCESS_EXPIRED_MINUTES), AccessConfig.DEFAULT_ACCESS_EXPIRED_MINUTES);
        
    	//revert the acessToken and check if the token is expired.
        AccessToken token = extractAccessToken(accessToken);
        if(token.getLastAccess()!=null)
        {
            if( TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - token.getLastAccess()) > expired_minutes)
            	throw new AuthException("AccessToken expired");
        }
        
    	//validated the user again.
        return authenticate(server, token.getUsername(), token.getPassword());
    }
    
    @POST
    @Path("/ldap")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse authenticatePost(@FormParam("server") String server, @FormParam("username") String username,
                                         @FormParam("password") String password) {
        return authenticate(server, username, password);
    }

    @Override
    public AuthResponse authenticate(String server, String username, String password) {
        logger.fine("New request recieved");

        if (server != null && !Configuration.isConfigured(server)) {
            throw new AuthException("No configuration found for server [" + server + "]");
        }

        if (StringUtils.isEmpty(username)) {
            throw new AuthException("Username not supplied");
        }

        try {

        	//generating the access token
        	AccessToken accessToken = new AccessToken(username, password, System.currentTimeMillis() );
        	
            if ("true".equalsIgnoreCase(Configuration.get(server, Keys.LDAP_BASE64))) {
                username = new String(Base64.decode(username));
                password = new String(Base64.decode(password));
            }

            boolean ad = !StringUtils.isEmpty(Configuration.get(server, Keys.LDAP_AD))
                    && "true".equalsIgnoreCase(Configuration.get(server, Keys.LDAP_AD));
            String sbase = Configuration.get(server, Keys.LDAP_SBASE);
            String domain = Configuration.get(server, Keys.LDAP_AD_DOMAIN);
            String principle; // dn

            if (ad) {
                if (StringUtils.isEmpty(domain)) {
                    principle = username;
                } else {
                    principle = domain + "\\" + username;
                }
            } else {
                principle = "uid=" + username + "," + sbase;
            }

            LdapAuthentication ldap = getLdap(server, username);

            logger.fine("Authenticating request");
            // authenticate
            Map<String, List<String>> lookupMap = ldap.authenticate(principle, password);

            AuthResponse response = new AuthResponse();
        	response.setAccessToken( accessToken.toString() );//associate the token just after the authentication
            
            if (lookupMap != null && !lookupMap.isEmpty()) {
                response.setLookup(lookupMap);
            }

            return response;
        } catch (Exception e) {
            throw new AuthException(e.getMessage(), e);
        }
    }

    private LdapAuthentication getLdap(String server, String username) {
        String host = Configuration.get(server, Keys.LDAP_HOST);
        int port = StringUtils.toInt(Configuration.get(server, Keys.LDAP_PORT), LdapConfig.DEFAULT_LDAP_PORT);
        boolean ad = !StringUtils.isEmpty(Configuration.get(server, Keys.LDAP_AD))
                && "true".equalsIgnoreCase(Configuration.get(server, Keys.LDAP_AD));
        String sbase = Configuration.get(server, Keys.LDAP_SBASE);
        String lookup = Configuration.get(server, Keys.LDAP_LOOKUP);
        String sfilter = Configuration.get(server, Keys.LDAP_SFILTER);

        LdapAuthentication ldap = new LdapAuthentication(host, port);

        if (!StringUtils.isEmpty(sbase)) {
            if (!ad) {
                sbase = "uid=" + username + "," + sbase;
            }

            ldap.setSearchBase(sbase);
        }
        
        if (!StringUtils.isEmpty(lookup)) {
            ldap.setLookupAttributes(lookup.split(","));
        } else {
            if (ad) {
                ldap.setLookupAttributes(LdapConfig.DEFAULT_AD_LOOKUP_ATTR);
            } else {
                ldap.setLookupAttributes(LdapConfig.DEFAULT_LDAP_LOOKUP_ATTR);
            }
        }

        if (!StringUtils.isEmpty(sfilter)) {
            ldap.setSearchFilter(sfilter.replaceAll("\\{username\\}", username));
        } else {
            if (ad) {
                ldap.setSearchFilter(LdapConfig.DEFAULT_AD_SFILTER.replaceAll("\\{username\\}", username));
            } else {
                ldap.setSearchFilter(LdapConfig.DEFAULT_LDAP_SFILTER);
            }
        }

        return ldap;
    }
    
    /***
     * Extract the Java Object from the access token 
     * 
     * @param response - AuthResponse object that will return
     * @param username - user name
     * @param password - password
     */
    private AccessToken extractAccessToken(String key)
    {
    	try {
			return new AccessToken(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
}
