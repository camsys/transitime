package org.transitclock.utils;

import org.transitclock.configData.AvlConfig;

import java.util.Set;

/**
 * Utility methods for filtering on routes.
 */
public class RouteFilterUtils {

    public static Set<String> getFilteredRoutes(){
        Set<String> allowedRoutes = StringUtils.parseSeparatedValues(AvlConfig.avlAllowedRoutes());
        if(!allowedRoutes.isEmpty()){
            allowedRoutes = allowedRoutes;
        } else {
            allowedRoutes.add("*");
        }
        return allowedRoutes;
    }

    public static boolean hasValidRoute(Set<String> routeFilterSet, String routeId) throws Exception{
        if(routeId == null || routeFilterSet.isEmpty() || (routeFilterSet.size() > 0 && routeFilterSet.contains("*")))
            return true;

        String route = routeId.toUpperCase();
        if (routeFilterSet.contains(route)) {
            return true;
        } else {
            return false;
        }

    }
}
