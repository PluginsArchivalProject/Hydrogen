package net.frozenorb.hydrogen.util;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionUtils {

    public static Map<String, Boolean> mergePermissions(Map<String, Boolean> current, Map<String, Boolean> merge) {
        if (merge == null)
            return new HashMap<>(current);

        return new HashMap<>(merge);
    }

    public static Map<String, Boolean> convertFromList(List<String> permissionsList) {
        if (permissionsList == null)
            return ImmutableMap.of();

        Map<String, Boolean> permissionsMap = new HashMap<>();
        permissionsList.forEach(permission -> {
            boolean b = permission.startsWith("-");
            permissionsMap.put(b ? permission.substring(1) : permission, !b);
        });
        return permissionsMap;
    }

    private PermissionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}