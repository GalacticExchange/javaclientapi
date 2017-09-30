package io.gex.core.model;

import org.apache.commons.lang3.StringUtils;

public class NodeUninstConfig {

    private String nodeName;
    private String nodeId;
    private SshCredentials sshCredentials;

    public SshCredentials getSshCredentials() {
        return sshCredentials;
    }

    public void setSshCredentials(SshCredentials sshCredentials) {
        this.sshCredentials = sshCredentials;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public static String validate(NodeUninstConfig nodeUninstConfig) {
        if (nodeUninstConfig == null) {
            return "Node uninstall config object must not me null.";
        } else if (nodeUninstConfig.nodeId == null) {
            return "Node id must not be empty.";
        } else if (StringUtils.isEmpty(nodeUninstConfig.nodeName)) {
            return "Node name must not be empty.";
        } else {
            return SshCredentials.validate(nodeUninstConfig.sshCredentials);
        }
    }
}
