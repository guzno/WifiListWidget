package se.magnulund.android.wifilistwidget.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 23/12/2012
 * Time: 22:00
 * To change this template use File | Settings | File Templates.
 */
public class ComponentManager {

    private static final String TAG = "ComponentManager";

    public static int getComponentEnabledSetting(Context context, ComponentName componentName) {

        return context.getPackageManager().getComponentEnabledSetting(componentName);
    }

    public static boolean enableComponent(Context context, Class<?> component) {

        ComponentName componentName = new ComponentName(context, component);
        int status = getComponentEnabledSetting(context, componentName);

        if (status == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            context.getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "Enabling " + componentName.getShortClassName());
            return true;
        }
        return false;
    }

    public static boolean disableComponent(Context context, Class<?> component) {

        ComponentName componentName = new ComponentName(context, component);
        int status = getComponentEnabledSetting(context, componentName);

        if (status < PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            context.getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "Disabling " + componentName.getShortClassName());
            return true;
        }
        return false;
    }
}
