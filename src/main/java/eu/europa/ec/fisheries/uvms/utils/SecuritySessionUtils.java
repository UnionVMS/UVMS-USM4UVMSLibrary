package eu.europa.ec.fisheries.uvms.utils;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.rest.security.AuthorizationFilter;
import eu.europa.ec.fisheries.wsdl.user.types.Dataset;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by georgige on 10/27/2015.
 */
public class SecuritySessionUtils {

    public static String getCachedUserPreference(String preferenceName, HttpSession session) {
        String value = null;
        if (session != null) {
            Object userPrefsObj = session.getAttribute(AuthConstants.HTTP_SESSION_ATTR_USER_PREFERENCES);

            if (userPrefsObj != null) {
                value = ((Map<String, String>)userPrefsObj).get(preferenceName);
            }
        }

        return value;
    }

    public void setUserPreference(String prefName, String prefValue, HttpSession session) {
        //TODO update/set the preference into USM and also refresh the map stored as a session attribute
        //we may have to transform this class into EJB  since we have to use the JMS producer and consumer
    }

    public List<String> getCachedDatasetsPerCategory(String category, HttpSession session) {
        //TODO something simillar as getCachedUserPreference but with Datasets instead
        //Use the discriminator or name as an unique field to define scope area restrictions (Area Type/Area ID)
        return null;
    }

    public static Set<String> getCachedUserFeatures(HttpSession session) {
        Set<String> features = null;

        if (session != null) {
            Object featuresObj =  session.getAttribute(AuthorizationFilter.HTTP_SESSION_ATTR_ROLES_NAME);

            if (featuresObj instanceof Set) {
                features = (Set<String>) featuresObj;
            }
        }

        return features;
    }
}
