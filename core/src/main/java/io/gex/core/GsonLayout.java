package io.gex.core;

import com.google.gson.JsonObject;
import io.gex.core.log.LogLevel;
import io.gex.core.log.LogSource;
import io.gex.core.log.SlackLogger;
import io.gex.core.model.properties.GexdProperties;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.model.properties.UserProperties;
import io.gex.core.propertiesHelper.UserPropertiesHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.nio.charset.Charset;
import java.util.TimeZone;

import static io.gex.core.AppContext.USERNAME_PARAM;

@Plugin(name = "GsonLayout", category = "Core", elementType = "layout", printObject = true)
public final class GsonLayout extends AbstractStringLayout {
    @SuppressWarnings("FieldCanBeLocal")
    private static String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static TimeZone timeZone = TimeZone.getTimeZone("UTC");
    public static SlackLogger slackLogger = null;

    protected GsonLayout(Charset charset) {
        super(charset);
    }

    @PluginFactory
    public static GsonLayout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
        return new GsonLayout(charset);
    }

    @Override
    public String toSerializable(LogEvent event) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("created_at", DateFormatUtils.format(event.getTimeMillis(), pattern, timeZone));
        ThrowableProxy throwableProxy = event.getThrownProxy();
        jsonObject.addProperty("throwable", throwableProxy != null ? throwableProxy.getCauseStackTraceAsString() : null);
        String instanceID = null;
        try {
            GexdProperties gexdProperties = PropertiesHelper.gexd.getProps();
            if (gexdProperties != null && StringUtils.isNotEmpty(gexdProperties.getInstanceID())) {
                instanceID = gexdProperties.getInstanceID();
            }
        } catch (Exception e) {
            // do nothing
        }
        //todo do not send instanceID for cli
        jsonObject.addProperty(GexdProperties.INSTANCE_ID_PROPERTY_NAME, instanceID);

        String nodeID = null;
        String clusterID = null;
        String username = null;
        if (PropertiesHelper.isService()) {
            try {
                NodeProperties nodeProperties = PropertiesHelper.node.getProps();
                if (nodeProperties != null) {
                    if (StringUtils.isNotBlank(nodeProperties.getClusterID())) {
                        clusterID = nodeProperties.getClusterID();
                    }
                    if (StringUtils.isNotBlank(nodeProperties.getNodeID())) {
                        nodeID = nodeProperties.getNodeID();
                    }
                }
            } catch (Exception e) {
                // do nothing
            }
        } else {
            try {
                username = event.getContextData().getValue(USERNAME_PARAM);
                username = StringUtils.defaultIfBlank(username, UserPropertiesHelper.getCurrentUsername());
            } catch (Exception e) {
                // do nothing
            }
        }
        jsonObject.addProperty(NodeProperties.NODE_ID_PROPERTY_NAME, nodeID);
        jsonObject.addProperty(NodeProperties.CLUSTER_ID_PROPERTY_NAME, clusterID);
        jsonObject.addProperty(UserProperties.USERNAME_PROPERTY_NAME, username);
        if (slackLogger != null && event.getLevel() == Level.ERROR) {
            slackLogger.send(event.getMessage().getFormattedMessage(), jsonObject);
        }
        JsonObject message = new JsonObject();
        message.addProperty("message", event.getMessage().getFormattedMessage());
        jsonObject.addProperty("data", message.toString());
        jsonObject.addProperty("logger", event.getLoggerName());
        jsonObject.addProperty("level", LogLevel.valueOf(event.getLevel().name()).getNumber());
        jsonObject.addProperty("source_id", PropertiesHelper.isService() ? LogSource.gexd.getNumber() : LogSource.cli.getNumber());
        jsonObject.addProperty("type_name", event.getMarker() != null ? event.getMarker().getName().toLowerCase() : null);
        return jsonObject.toString();
    }
}
