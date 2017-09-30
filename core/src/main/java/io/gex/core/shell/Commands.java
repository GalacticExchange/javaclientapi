package io.gex.core.shell;

import org.apache.commons.lang3.SystemUtils;

import java.util.Arrays;
import java.util.List;

public class Commands {

    public final static String BOX_NAME = "gex/client";
    public final static String BOX_NAME_SLASH = "gex-VAGRANTSLASH-client";
    public final static String PATH = "PATH";
    public final static String PATH_WIN = "Path";
    public final static String REDIRECT_OUTPUT = " 2>&1";
    public final static String VAGRANT_HOME = "VAGRANT_HOME";
    public final static String VAGRANT_PROVISION_INSTALL = " -- provision --provision-with install_container";
    public final static String VAGRANT_PROVISION_RUN = " -- provision --provision-with run_container";
    public final static String VAGRANT_PROVISION_UNINSTALL = " -- provision --provision-with remove_container";
    public final static String VAGRANT_PROVISION_CHANGE = " -- provision --provision-with change_container_state";
    public final static String VAGRANT = "vagrant";
    public final static String VAGRANT_BOX_LIST = "vagrant box list";
    public final static String APP_NAME = " --app-name=";
    public final static String IMAGE_FILE = " --image-file=";
    public final static String JSON_FILE = " --json-file=";
    public final static String CONTAINER_NAME = " --container-name=";
    public final static String ACTION= " --action=";
    public final static String CONTAINER_NAME_ENV = " container_name=";
    public final static String ACTION_ENV = " action=";
    public final static String CHANGE_CONTROLLER_STATE_PATH = " ruby /home/vagrant/ruby_scripts/change_container_state.rb";
    public final static String VAGRANT_V = "vagrant -v";
    public final static String VAGRANT_UP = "vagrant up --provider virtualbox";
    public final static String VAGRANT_HALT = "vagrant halt";
    public final static String VAGRANT_DESTROY_F = "vagrant destroy -f";
    public final static String VAGRANT_STATUS = "vagrant status --machine-readable";

    public static List<String> exec(String str) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return cmd(str);
        } else {
            return bash(str);
        }
    }

    public static List<String> cmd(String str) {
        return Arrays.asList("cmd.exe", "/C", str);
    }

    public static List<String> bash(String str) {
        return Arrays.asList("/bin/bash", "-c", str);
    }

    public static List<String> osascript(String str) {
        return Arrays.asList("osascript", "-e", str);
    }

    public static String vagrantAddBox(String boxPath) {
        return "vagrant box add " + BOX_NAME + " \"" + boxPath + "\" --force";
    }

}
