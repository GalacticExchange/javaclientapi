package io.gex.core.model;


import org.apache.commons.lang3.StringUtils;

public class NodeInstConfig {
    private String nodeName;
    private String hadoopApp;
    private SshCredentials sshCredentials;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public SshCredentials getSshCredentials() {
        return sshCredentials;
    }

    public void setSshCredentials(SshCredentials sshCredentials) {
        this.sshCredentials = sshCredentials;
    }

    public String getHadoopApp() {
        return hadoopApp;
    }

    public void setHadoopApp(String hadoopApp) {
        this.hadoopApp = hadoopApp;
    }

    public static String validate(NodeInstConfig nodeInstConfig) {
        if (nodeInstConfig == null) {
            return "Node install config object must not me null.";
        } else if (StringUtils.isEmpty(nodeInstConfig.hadoopApp)) {
            return "HadoopApp property must not be empty.";
        } else if (StringUtils.isEmpty(nodeInstConfig.nodeName)) {
            return "Node name must not be empty.";
        } else {
            return SshCredentials.validate(nodeInstConfig.sshCredentials);
        }
    }
}
