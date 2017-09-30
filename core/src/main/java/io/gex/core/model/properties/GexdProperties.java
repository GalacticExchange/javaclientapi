package io.gex.core.model.properties;

import io.gex.core.model.EntityType;

public class GexdProperties extends BaseProperties {

    private String webServerPort;
    private String instanceID;
    private EntityType nodeType;

    public final static String WEB_SERVER_PORT_PROPERTY_NAME = "webServerPort";
    public final static String INSTANCE_ID_PROPERTY_NAME = "instanceID";
    public final static String NODE_TYPE_PROPERTY_NAME = "nodeType";

    public String getWebServerPort() {
        return webServerPort;
    }

    public void setWebServerPort(String webServerPort) {
        this.webServerPort = webServerPort;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public EntityType getNodeType() {
        return nodeType;
    }

    public void setNodeType(EntityType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public GexdProperties copy() {
        GexdProperties res = new GexdProperties();
        res.webServerPort = this.webServerPort;
        res.instanceID = this.instanceID;
        res.nodeType = this.nodeType;
        return res;
    }
}
