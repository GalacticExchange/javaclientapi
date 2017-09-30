package io.gex.agent;


import io.gex.core.PropertiesHelper;
import org.apache.commons.lang3.StringUtils;

public class GexdHelper {

    public static void sleep() {
        sleep(3000);
    }

    // in milliseconds
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            // do nothing
        }
    }

    public static String waitForProperty(String name) {
        String value;
        while (true) {
            try {
                value = PropertiesHelper.node.get(name);
                if (StringUtils.isBlank(value)) {
                    throw new Exception();
                }
            } catch (Exception e) {
                sleep(60000);
                continue;
            }
            break;
        }
        return value;
    }

}
