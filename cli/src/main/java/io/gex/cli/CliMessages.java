package io.gex.cli;

import io.gex.core.CoreMessages;
import io.gex.core.ValidationHelper;
import io.gex.core.log.LogHelper;
import io.gex.core.model.Color;
import io.gex.core.vagrantHelper.VagrantHelper;
import io.gex.core.virutalBoxHelper.VirtualBoxHelper;
import io.gex.core.virutalBoxHelper.VirtualBoxHelperWindows;
import org.apache.commons.lang3.StringUtils;

class CliMessages {

    //short
    final static String DEBUG = "-d";
    final static String FORCE = "-f";
    final static String FORCE_YES = "-y";
    final static String TOKEN = "--token=";
    final static String UI = "--ui";
    final static String HELP_LONG = "--help";
    final static String HELP_SHORT = "-h";
    final static String VERSION_LONG = "--version";
    final static String VERSION_SHORT = "-v";
    final static String API_LONG = "--api";
    final static String PASSWORD_SHORT = "-p";

    // common
    final static String DELIMITER = StringUtils.repeat("=", 80);
    final static String UNKNOWN_COMMAND = "Unknown command.";
    final static String USAGE = "Usage: ";
    final static String DEBUG_DESCRIPTION = "Enable debugging";
    final static String FORCE_YES_DESCRIPTION = "Assume Yes to all queries and do not prompt";
    final static String HELP = "Show this screen";
    final static String VERSION = "Show version";
    final static String API = "Show API version";
    final static String ENTER_PASSWORD = "\n\tPlease enter your password: ";
    final static String USERNAME_EMAIL = "\n\tPlease enter your username: ";
    final static String PASSWORD_CONFIRM = "\n\tConfirm password: ";
    final static String PASSWORDS_MATCH_ERROR = "Passwords do not match.";
    final static String COMMAND_EXECUTED = "Command completed.";
    final static String UNEXPECTED_END = "Unexpected end of a program.";
    final static String ASCII_ERROR = "Failed to print ASCII image.";
    final static String INVALID_INPUT = "Invalid input.";
    final static String EMPTY_INPUT = "Invalid input. Should not be empty.";
    final static String INVALID_INPUT_TRY = "Invalid input. Try again:";
    final static String WRONG_PASSWORD_TRY = "Wrong password. Try again:";
    final static String ROOT_PASSWORD = "Enter root password:";
    final static String READ_CONSOLE_ERROR = "Failed to read console output.";
    final static String WORKING_DIRECTORY_RESTRICTION = "Current working directory will be removed during node reinstallation. \n\tPlease change your working directory.";
    final static String RABBIT_CHECK = "Checking message server ...";
    final static String GEXD_CHECK = "Checking ClusterGX service ...";
    final static String NETWORK_CHECK = "Checking network connection ...";
    final static String VAGRANT_BOX_CHECK = "Checking Vagrant ...";
    final static String DKMS_CHECK = "Checking DKMS ...";
    final static String NODE_INST_SPACE_CHECK = "Checking free space to install node ...";
    final static String DONE = "Done.";
    final static String INVITATIONS_EMPTY = "There are no invitations available.";
    final static String SELECT_ADAPTER = "Select network adapter";
    final static String SELECT_SERVICE = "Select service";
    final static String GENERAL_EXCEPTION = "General exception: ";

    // Vagrant
    final static String VAGRANT_START_HELP_SYMBOL = "vagrant ";
    private final static String VAGRANT_COMMAND_NAME = "vagrant ";
    //install
    final static String VAGRANT_INSTALL_COMMAND = "install";
    final static String VAGRANT_INSTALL_DESCRIPTION = "Install Vagrant";
    final static String VAGRANT_INSTALL_PARAMS = VAGRANT_COMMAND_NAME + VAGRANT_INSTALL_COMMAND;
    //check
    final static String VAGRANT_CHECK_COMMAND = "check";
    final static String VAGRANT_CHECK_DESCRIPTION = "Check Vagrant version compatibility";
    final static String VAGRANT_CHECK_PARAMS = VAGRANT_COMMAND_NAME + VAGRANT_CHECK_COMMAND;

    // VirtualBox
    final static String VIRTUAL_BOX_START_HELP_SYMBOL = "virtualbox ";
    private final static String VIRTUAL_BOX_COMMAND_NAME = "virtualbox ";
    //install
    final static String VIRTUAL_BOX_INSTALL_COMMAND = "install";
    final static String VIRTUAL_BOX_INSTALL_DESCRIPTION = "Install VirtualBox";
    final static String VIRTUAL_BOX_INSTALL_PARAMS = VIRTUAL_BOX_COMMAND_NAME + VIRTUAL_BOX_INSTALL_COMMAND;
    //check
    final static String VIRTUAL_BOX_CHECK_COMMAND = "check";
    final static String VIRTUAL_BOX_CHECK_DESCRIPTION = "Check VirtualBox  version compatibility";
    final static String VIRTUAL_BOX_CHECK_PARAMS = VIRTUAL_BOX_COMMAND_NAME + VIRTUAL_BOX_INSTALL_COMMAND;

    // **************************************************************************************************************
    // VAGRANT
    // **************************************************************************************************************
    final static String INSTALLING_VAGRANT = "Installing Vagrant.";
    final static String VAGRANT_IS_NOT_INSTALLED = "Vagrant is not installed.";
    final static String VAGRANT_IS_INSTALLED = "Vagrant is installed.";
    final static String VAGRANT_INSTALLED = "Vagrant has been installed.";
    final static String VAGRANT_INSTALLATION_START = "Start Vagrant installation.";
    final static String VAGRANT_CHECK = "Checking Vagrant ...";
    final static String VAGRANT_AND_VIRTUAL_BOX_EXPLANATION = "We will now install Virtual Box and Vagrant packages. " +
            "These are open sources packages used to run virtual nodes.";
    final static String VAGRANT_VERSION = "You use the old version of Vagrant. Version " + VagrantHelper.VERSION +
            " or higher is required.";


    // **************************************************************************************************************
    // END OF A VAGRANT
    // **************************************************************************************************************

    // **************************************************************************************************************
    // VIRTUALBOX
    // **************************************************************************************************************
    final static String INSTALLING_VIRTUAL_BOX = "Installing VirtualBox.";
    final static String VIRTUAL_BOX_INSTALLED = "VirtualBox has been installed.";
    final static String VIRTUAL_BOX_INSTALLATION_START = "Start VirtualBox installation.";
    final static String VIRTUAL_BOX_CHECK = "Checking VirtualBox ...";
    final static String VIRTUAL_BOX_HOME_QUESTION = "Node will be installed to the default directory:\n\n\t"
            + VirtualBoxHelperWindows.DEFAULT_VBOX_USER_HOME + "\n\n\tSet custom machine folder on Enter to proceed: ";
    final static String VIRTUALIZATION_CHECK = "Checking virtualization settings ...";
    final static String HYPERV_CHECK = "Checking Hyper-V ...";
    final static String FREE_SPACE_DEP_CHECK = "Checking free space for installing dependencies ...";
    final static String CPU_CHECK = "Checking CPU ...";
    final static String RAM_CHECK = "Checking memory ...";
    final static String LOOK_FOR_NETWORK_CONNECTIONS = "Looking for network connections ...";
    final static String FOUND_NETWORK_CONNECTION = "Found network connection:";
    final static String WIFI_ADAPTER_CHOSEN = "Wi-Fi adapter has been chosen. Node will work in single mode.";
    final static String VIRTUAL_BOX_VERSION = "You use the old version of VirtualBox. Version " + VirtualBoxHelper.VERSION +
            " or higher is required.";
    final static String VIRTUAL_BOX_IS_NOT_INSTALLED = "VirtualBox is not installed.";
    final static String VIRTUAL_BOX_IS_INSTALLED = "VirtualBox is installed.";
    // **************************************************************************************************************
    // END OF A VIRTUALBOX
    // **************************************************************************************************************

    // **************************************************************************************************************
    // LOG
    // **************************************************************************************************************
    // file
    final static String LOG_FILE_COMMAND = "file";
    final static String LOG_FILE_PARAMS = LOG_FILE_COMMAND + " [--nodeID=<nodeID>] [--userToken=<token>]";
    // **************************************************************************************************************
    // END OF LOG
    // **************************************************************************************************************

    // **************************************************************************************************************
    // INVITATION
    // **************************************************************************************************************
    final static String INVITE_REMOVED = "Invitation has been removed.";
    final static String INVITE_ID_ERROR = "Invalid invitation id: ";
    final static String INVITE_START_HELP_SYMBOL = "invite ";
    private final static String INVITE_COMMAND_NAME = "invite ";
    final static String INVITE = "Invitation sent.";
    // remove
    final static String INVITE_REMOVE_COMMAND = "remove";
    final static String INVITE_REMOVE_DESCRIPTION = "Remove user or share invitation";
    final static String INVITE_REMOVE_PARAMS = INVITE_COMMAND_NAME + INVITE_REMOVE_COMMAND + " <id>";
    // userlist
    final static String INVITE_USERLIST_COMMAND = "userlist";
    final static String INVITE_USERLIST_DESCRIPTION = "Get list of user invitations";
    final static String INVITE_USERLIST_PARAMS = INVITE_COMMAND_NAME + INVITE_USERLIST_COMMAND;
    // user
    final static String INVITE_USER_COMMAND = "user";
    final static String INVITE_USER_DESCRIPTION = "Invite user to join the team";
    final static String INVITE_USER_PARAMS = INVITE_COMMAND_NAME + INVITE_USER_COMMAND + " <email>";
    // share
    final static String INVITE_SHARE_COMMAND = "share";
    final static String INVITE_SHARE_DESCRIPTION = "Invite user to share the cluster";
    final static String INVITE_SHARE_PARAMS = INVITE_COMMAND_NAME + INVITE_SHARE_COMMAND + " --email=<email> --clusterID=<clusterID>";
    // sharelist
    final static String INVITE_SHARELIST_COMMAND = "sharelist";
    final static String INVITE_SHARELIST_DESCRIPTION = "Get list of invitations to share the cluster";
    final static String INVITE_SHARELIST_PARAMS = INVITE_COMMAND_NAME + INVITE_SHARELIST_COMMAND + " <clusterID>";
    // **************************************************************************************************************
    // END OF A INVITATION
    // **************************************************************************************************************

    // **************************************************************************************************************
    // APPLICATION
    // **************************************************************************************************************
    final static String APPLICATION_EMPTY = "There are no applications available.";
    final static String APP_START_HELP_SYMBOL = "app ";
    private final static String APP_COMMAND_NAME = "app ";

    // install
    final static String APP_INSTALL_START = "Application installation started. It may take some time to complete.";
    final static String APP_INSTALL_COMMAND = "install";
    final static String APP_INSTALL_DESCRIPTION = "Install application on node";
    final static String APP_INSTALL_PARAMS = APP_COMMAND_NAME + APP_INSTALL_COMMAND + " <application name>";

    // uninstall
    final static String APP_UNINSTALL_START = "Application uninstallation started. It may take some time to complete.";
    final static String APP_UNINSTALL_COMMAND = "uninstall";
    final static String APP_UNINSTALL_DESCRIPTION = "Uninstall application on node";
    final static String APP_UNINSTALL_PARAMS = APP_COMMAND_NAME + APP_UNINSTALL_COMMAND + " <applicationID>";

    // list
    final static String APP_LIST_COMMAND = "list";
    final static String APP_LIST_DESCRIPTION = "Get installed applications for specified cluster";
    final static String APP_LIST_PARAMS = APP_COMMAND_NAME + APP_LIST_COMMAND + " <clusterID>";

    // supported
    final static String APP_SUPPORTED_COMMAND = "supported";
    final static String APP_SUPPORTED_DESCRIPTION = "Get list of supported applications for specified cluster";
    final static String APP_SUPPORTED_PARAMS = APP_COMMAND_NAME + APP_SUPPORTED_COMMAND + " <clusterID>";
    // **************************************************************************************************************
    // END OF A APPLICATION
    // **************************************************************************************************************

    // **************************************************************************************************************
    // UPDATE
    // **************************************************************************************************************
    final static String UPDATE_START_HELP_SYMBOL = "update ";
    private final static String UPDATE_COMMAND_NAME = "update ";
    final static String NO_UPDATES = "Nothing to update\n\tYou are using the most recent version of ClusterGX.";
    final static String NEW_VERSION_TEMPLATE = "A new version " + CoreMessages.DATA_TEMPLATE + " is available.";
    final static String NEW_VERSION = "A new version  is available.";
    final static String UPDATE_QUESTION = "\tStart updating  [Y/n]?";
    final static String UPDATE_DOWNLOADED = "Updates downloaded.";
    final static String UPDATER_STARTED = "Updater started.";
    final static String DOWNLOADING_UPDATER = "Downloading updater ... ";
    final static String DOWNLOADING_DISTRIBUTION = "Downloading distribution ... ";
    //install
    final static String UPDATE_INSTALL_COMMAND = "install";
    final static String UPDATE_INSTALL_DESCRIPTION = "Install update";
    final static String UPDATE_INSTALL_PARAMS = UPDATE_COMMAND_NAME + UPDATE_INSTALL_COMMAND;
    //check
    final static String UPDATE_CHECK_COMMAND = "check";
    final static String UPDATE_CHECK_DESCRIPTION = "Check for updates";
    final static String UPDATE_CHECK_PARAMS = UPDATE_COMMAND_NAME + UPDATE_CHECK_COMMAND;
    // **************************************************************************************************************
    // END OF A UPDATE
    // **************************************************************************************************************

    // **************************************************************************************************************
    // SSH
    // **************************************************************************************************************
    final static String SSH_START_HELP_SYMBOL = "ssh ";
    private final static String SSH_COMMAND_NAME = "ssh ";
    final static String SELECT_CLUSTER = "Select shared cluster:";
    final static String NO_CLUSTERS = "No available clusters found.";
    final static String NO_SHARED_CLUSTERS = "No available shared clusters found.";
    final static String OPTIONS = "Options: ";
    final static String OPTIONS_PARAMETER = "[options]";

    // ssh
    final static String SSH_BLANK_DESCRIPTION = "Connect to localhost/master via ssh";
    //master
    final static String SSH_MASTER_COMMAND = "master";
    final static String SSH_MASTER_DESCRIPTION = "Connect to master via ssh";
    final static String SSH_MASTER_PARAMS = SSH_COMMAND_NAME + SSH_MASTER_COMMAND + " [--native]";
    //shared
    final static String SSH_SHARED_COMMAND = "shared";
    final static String SSH_SHARED_DESCRIPTION = "Connect to node from shared cluster via ssh";
    final static String SSH_SHARED_PARAMS = SSH_COMMAND_NAME + SSH_SHARED_COMMAND + " [--native]";
    //shared
    final static String SSH_HOST_COMMAND = "host";
    final static String SSH_HOST_DESCRIPTION = "Connect to remote host via ssh";
    final static String SSH_HOST_PARAMS = SSH_COMMAND_NAME + SSH_HOST_COMMAND +
            " --host=<host> --port=<port> --username=<username> --password=<password> " +
            "[--proxy=<proxy>[ --proxyUsername=<username> --proxyPassword=<password>]]";
    //native
    final static String SSH_NATIVE_COMMAND = "--native";
    final static String SSH_NATIVE_DESCRIPTION = "Open ssh connection via native application";

    // **************************************************************************************************************
    // END OF A SSH
    // **************************************************************************************************************

    // **************************************************************************************************************
    // MAIN
    // **************************************************************************************************************
    final static String LOGIN_MESSAGE = "\n\tWelcome back!\n";
    // login
    final static String LOGIN_COMMAND = "login";
    final static String LOGIN_DESCRIPTION = "Login";
    final static String LOGIN_PARAMS = LOGIN_COMMAND + " <username/email>";
    // logout
    final static String LOGOUT_COMMAND = "logout";
    final static String LOGOUT_DESCRIPTION = "Logout";
    final static String LOGOUT_PARAMS = LOGOUT_COMMAND;
    // user
    final static String LOG_COMMAND = "log";
    // user
    final static String USER_COMMAND = "user";
    final static String USER_DESCRIPTION = "User commands";
    // cluster
    final static String CLUSTER_COMMAND = "cluster";
    final static String CLUSTER_DESCRIPTION = "Cluster commands";
    // node
    final static String NODE_COMMAND = "node";
    final static String NODE_DESCRIPTION = "Node commands";
    // node
    final static String APPLICATION_COMMAND = "app";
    final static String APPLICATION_DESCRIPTION = "Application commands";
    // service
    final static String SHARE_COMMAND = "share";
    final static String SHARE_DESCRIPTION = "Sharing commands";
    // team
    final static String TEAM_COMMAND = "team";
    final static String TEAM_DESCRIPTION = "Team commands";
    // invite
    final static String INVITE_COMMAND = "invite";
    final static String INVITE_DESCRIPTION = "Invitation commands";
    // update
    final static String UPDATE_COMMAND = "update";
    final static String UPDATE_DESCRIPTION = "Update commands";
    // ssh
    final static String SSH_COMMAND = "ssh";
    final static String SSH_DESCRIPTION = "Connect to node via ssh";
    // VirtualBox
    final static String VIRTUAL_BOX_COMMAND = "virtualbox";
    final static String VIRTUAL_BOX_DESCRIPTION = "VirtualBox commands";
    // Vagrant
    final static String VAGRANT_COMMAND = "vagrant";
    final static String VAGRANT_DESCRIPTION = "Vagrant commands";

    // **************************************************************************************************************
    // END OF A MAIN
    // **************************************************************************************************************

    // **************************************************************************************************************
    // CLUSTER
    // **************************************************************************************************************
    final static String CLUSTER_EMPTY = "There are no clusters available.";
    final static String CLUSTER_START_HELP_SYMBOL = "cluster ";
    private final static String CLUSTER_COMMAND_NAME = "cluster ";
    // create
    final static String CLUSTER_CREATE_COMMAND = "create";
    final static String CLUSTER_CREATE_DESCRIPTION = "Create cluster";
    final static String CLUSTER_CREATE_PARAMS = CLUSTER_COMMAND_NAME + CLUSTER_CREATE_COMMAND + " --hadoopType=<hadoopType>" +
            " (--clusterType=aws --awsKeyId=<awsKeyId> --awsSecretKey=<awsSecretKey> --awsRegion=<awsRegion> |" +
            " --clusterType=onprem [--proxyIP=<IP>] [--staticIPs=(true|false) --gatewayIP=<gatewayIP>" +
            " --networkIPRangeStart=<start> --networkIPRangeEnd=<end> --networkMask=<mask>]" +
            " [--proxyUser=<proxyUser> --proxyPassword=<proxyPassword>])";
    // info
    final static String CLUSTER_INFO_COMMAND = "info";
    final static String CLUSTER_INFO_DESCRIPTION = "Get cluster info";
    final static String CLUSTER_INFO_PARAMS = CLUSTER_COMMAND_NAME + CLUSTER_INFO_COMMAND + " <clusterID>";
    //list
    final static String CLUSTER_LIST_COMMAND = "list";
    final static String CLUSTER_LIST_DESCRIPTION = "Get cluster list";
    final static String CLUSTER_LIST_PARAMS = CLUSTER_COMMAND_NAME + CLUSTER_LIST_COMMAND;
    // **************************************************************************************************************
    // END OF A CLUSTER
    // **************************************************************************************************************

    // **************************************************************************************************************
    // TEAM
    // **************************************************************************************************************
    final static String TEAM_UPDATED = "Team info has been updated.";
    final static String TEAM_START_HELP_SYMBOL = "team ";
    private final static String TEAM_COMMAND_NAME = "team ";
    // info
    final static String TEAM_INFO_COMMAND = "info";
    final static String TEAM_INFO_DESCRIPTION = "Get team info";
    final static String TEAM_INFO_PARAMS = TEAM_COMMAND_NAME + TEAM_INFO_COMMAND;
    // update
    final static String TEAM_UPDATE_COMMAND = "update";
    final static String TEAM_UPDATE_DESCRIPTION = "Update team profile";
    final static String TEAM_UPDATE_PARAMS = TEAM_COMMAND_NAME + TEAM_UPDATE_COMMAND + " --about=<about>";
    // clusters
    final static String TEAM_CLUSTERS_COMMAND = "clusters";
    final static String TEAM_CLUSTERS_DESCRIPTION = "Get team cluster list";
    final static String TEAM_CLUSTERS_PARAMS = TEAM_COMMAND_NAME + TEAM_CLUSTERS_COMMAND;
    // users
    final static String TEAM_USERS_COMMAND = "users";
    final static String TEAM_USERS_DESCRIPTION = "Get team user list";
    final static String TEAM_USERS_PARAMS = TEAM_COMMAND_NAME + TEAM_USERS_COMMAND;
    // **************************************************************************************************************
    // END OF A TEAM_CAPS
    // **************************************************************************************************************

    // **************************************************************************************************************
    // SHARE
    // **************************************************************************************************************
    final static String SHARE_CREATE = "The cluster share has been created.";
    final static String SHARE_REMOVE = "The cluster share has been removed.";
    final static String SHARE_START_HELP_SYMBOL = "share ";
    private final static String SHARE_COMMAND_NAME = "share ";
    // create
    final static String SHARE_CREATE_COMMAND = "create";
    final static String SHARE_CREATE_DESCRIPTION = "Share this cluster with a user";
    final static String SHARE_CREATE_PARAMS = SHARE_COMMAND_NAME + SHARE_CREATE_COMMAND + " --username=<username> --clusterID=<clusterID>";
    // remove
    final static String SHARE_REMOVE_COMMAND = "remove";
    final static String SHARE_REMOVE_DESCRIPTION = "Stop sharing this cluster with a user";
    final static String SHARE_REMOVE_PARAMS = SHARE_COMMAND_NAME + SHARE_REMOVE_COMMAND + " --username=<username> --clusterID=<clusterID>";
    // userlist
    final static String SHARE_USERLIST_COMMAND = "userlist";
    final static String SHARE_USERLIST_DESCRIPTION = "Get list of users who share the cluster";
    final static String SHARE_USERLIST_PARAMS = SHARE_COMMAND_NAME + SHARE_USERLIST_COMMAND + " <clusterID>";
    // clusterlist
    final static String SHARE_CLUSTERLIST_COMMAND = "clusterlist";
    final static String SHARE_CLUSTERLIST_DESCRIPTION = "Get list of shared clusters";
    final static String SHARE_CLUSTERLIST_PARAMS = SHARE_COMMAND_NAME + SHARE_CLUSTERLIST_COMMAND;
    // **************************************************************************************************************
    // END OF A SHARE
    // **************************************************************************************************************

    // **************************************************************************************************************
    // NODE
    // **************************************************************************************************************
    final static String NODE_EMPTY = "There are no cluster nodes available.";
    final static String NODE_START_HELP_SYMBOL = "node ";
    private final static String NODE_COMMAND_NAME = "node ";
    // install
    final static String NODE_INSTALL_COMMAND = "install";
    final static String NODE_INSTALL_DESCRIPTION = "Install node";
    final static String NODE_INSTALL_PARAMS = NODE_COMMAND_NAME + NODE_INSTALL_COMMAND + " --clusterID=<clusterID> [--name=<nodeName>]";
    final static String NODE_INSTALLING = "Node is installing. Please wait.";
    // reinstall
    final static String NODE_REINSTALL_COMMAND = "reinstall";
    final static String NODE_REINSTALL_DESCRIPTION = "Reinstall node";
    final static String NODE_REINSTALL_PARAMS = NODE_COMMAND_NAME + NODE_REINSTALL_COMMAND + " [-f]";
    final static String NODE_REINSTALL_QUESTION = "\tThis is a dangerous operation!!!  \tThis action will destroy the " +
            "node and delete all node data from your machine \n\tDo you really want to reinstall the node? [Y/n]: ";
    // commands
    final static String NODE_STOP = "stop";
    final static String NODE_START = "start";
    final static String NODE_RESTART = "restart";
    final static String NODE_STOP_DESCRIPTION = "Stop node";
    final static String NODE_START_DESCRIPTION = "Start node";
    final static String NODE_RESTART_DESCRIPTION = "Restart node";
    final static String NODE_COMMANDS_PARAMS = NODE_COMMAND_NAME + CoreMessages.DATA_TEMPLATE + " [<nodeID>]";
    final static String NODE_COMMANDS_ADDITIONAL_PARAMS = "If <nodeID> is not set command will be applied to the local node";
    final static String INVALID_COMMAND = "Invalid command.\nAppropriate commands are: start, stop or restart.";
    // env check
    final static String NODE_ENV_COMMAND = "env";
    final static String NODE_ENV_DESCRIPTION = "Check minimum system requirements to create node";
    final static String NODE_ENV_PARAMS = NODE_COMMAND_NAME + NODE_ENV_COMMAND + "[--afterDep [--vboxdir=<vboxdir>]]";
    final static String NODE_ENV_AFTER_DEP = "--afterDep";
    final static String NODE_ENV_VBOX_DIR = "--vboxdir=";
    // remove
    final static String NODE_REMOVE_COMMAND = "remove";
    final static String NODE_REMOVE_DESCRIPTION = "Remove node from cluster but keep all files";
    final static String NODE_REMOVE_PARAMS = NODE_COMMAND_NAME + NODE_REMOVE_COMMAND + " <nodeID>";
    final static String NODE_REMOVE_QUESTION = "\tThis is a dangerous operation!!! Removing the node will only delete " +
            "information about the node from our servers.\n\tThe files on your local machine will not be deleted. Do you really " +
            "want to remove the node? [Y/n]: ";
    final static String NODE_REMOVED = "A cluster node has been removed from your machine.";
    // uninstall
    final static String NODE_UNINSTALL_COMMAND = "uninstall";
    final static String NODE_UNINSTALL_DESCRIPTION = "Uninstall node and remove all files";
    final static String NODE_UNINSTALL_PARAMS = NODE_COMMAND_NAME + NODE_UNINSTALL_COMMAND + " [-f]";
    final static String NODE_UNINSTALL_QUESTION = "\tThis is a dangerous operation!!!  \tThis action will destroy the " +
            "node and delete all node data from your machine \n\tDo you really want to uninstall the node? [Y/n]: ";
    final static String UNINSTALLING = "Uninstalling ... Please wait ...";
    // list
    final static String NODE_LIST_COMMAND = "list";
    final static String NODE_LIST_DESCRIPTION = "Get node list";
    final static String NODE_LIST_PARAMS = NODE_COMMAND_NAME + NODE_LIST_COMMAND + " <clusterID>";
    // info
    final static String NODE_INFO_COMMAND = "info";
    final static String NODE_INFO_DESCRIPTION = "Get node info";
    final static String NODE_INFO_PARAMS = NODE_COMMAND_NAME + NODE_INFO_COMMAND + " <nodeID>";

    // **************************************************************************************************************
    // END OF A NODE
    // **************************************************************************************************************

    // **************************************************************************************************************
    // USER
    // **************************************************************************************************************
    final static String USER_EMPTY = "There are no users available.";
    final static String USER_UPDATED = "User info has been updated.";
    final static String USER_CHANGE_ROLE = "User role has been updated.";
    final static String USER_ROLES = "Invalid role. Appropriate roles are: admin or user.";
    final static String OLD_PASSWORD = "\tPlease enter old password: ";
    final static String NEW_PASSWORD = "\tPlease enter new password: ";
    final static String USER_PASSWORD_CHANGED = "Password has been changed.";
    final static String USER_REMOVE = "User has been removed.";
    final static String USER_PASSWORD = "\tPlease enter the user password: ";
    final static String USER_VERIFY = "\n\tVerifying user, please wait ...\n";
    final static String PASSWORD = "password";
    final static String USER_START_HELP_SYMBOL = "user ";
    private final static String USER_COMMAND_NAME = "user ";
    // reset password
    final static String USER_RESET_PASSWORD_LINK_COMMAND = "reset";
    final static String USER_RESET_PASSWORD_LINK_DESCRIPTION = "Reset password";
    final static String USER_RESET_PASSWORD_LINK_PARAMS = USER_COMMAND_NAME + PASSWORD + " " + USER_RESET_PASSWORD_LINK_COMMAND
            + " <username/email>";
    // confirm password
    final static String USER_CONFIRM_PASSWORD_COMMAND = "confirm";
    final static String USER_CONFIRM_PASSWORD_DESCRIPTION = "Reset password confirmation";
    final static String USER_CONFIRM_PASSWORD_PARAMS = USER_COMMAND_NAME + PASSWORD + " " + USER_CONFIRM_PASSWORD_COMMAND
            + " <token>";
    // change role
    final static String USER_CHANGE_ROLE_COMMAND = "role";
    final static String USER_CHANGE_ROLE_DESCRIPTION = "Change user role. User role can be admin or user";
    final static String USER_CHANGE_ROLE_PARAMS = USER_COMMAND_NAME + USER_CHANGE_ROLE_COMMAND
            + " <username>  <role>";
    // remove
    final static String USER_REMOVE_COMMAND = "remove";
    final static String USER_REMOVE_DESCRIPTION = "Remove user from the cluster";
    final static String USER_REMOVE_PARAMS = USER_COMMAND_NAME + USER_REMOVE_COMMAND + " <username>";
    // change password
    final static String USER_CHANGE_PASSWORD_COMMAND = "change";
    final static String USER_CHANGE_PASSWORD_DESCRIPTION = "Change password";
    final static String USER_CHANGE_PASSWORD_PARAMS = USER_COMMAND_NAME + PASSWORD + " " + USER_CHANGE_PASSWORD_COMMAND
            + " [<username>]";
    // info
    final static String USER_INFO_COMMAND = "info";
    final static String USER_INFO_DESCRIPTION = "Get user info";
    final static String USER_INFO_PARAMS = USER_COMMAND_NAME + USER_INFO_COMMAND + " [<username>]";
    // update
    final static String USER_UPDATE_COMMAND = "update";
    final static String USER_UPDATE_DESCRIPTION = "Edit user profile";
    final static String USER_UPDATE_PARAMS = USER_COMMAND_NAME + USER_UPDATE_COMMAND
            + " [--firstname=<firstname>] [--lastname=<lastname>] [--about=<about>]";
    // create
    final static String USER_CREATE_COMMAND = "create";
    final static String USER_CREATE_DESCRIPTION = "Create user";
    final static String USER_CREATE_PARAMS = USER_COMMAND_NAME + USER_CREATE_COMMAND +
            " [--verifyToken=<token>] --teamname=<teamname> --firstname=<firstname> --lastname=<lastname> --username=<username> --email=<email> --phoneNumber=<phoneNumber> [--password=<password>]";
    // verify
    final static String USER_VERIFY_COMMAND = "verify";
    final static String USER_VERIFY_DESCRIPTION = "Verify user";
    final static String USER_VERIFY_PARAMS = USER_COMMAND_NAME + USER_VERIFY_COMMAND + " <token>";
    // **************************************************************************************************************
    // END OF A USER
    // **************************************************************************************************************

    // printing
    final static String ID = "ID: ";
    final static String IP = "IP: ";
    final static String EMAIL = "Email: ";
    final static String USERNAME = "Username: ";
    final static String LAST_NAME = "Last name: ";
    final static String FIRST_NAME = "First name: ";
    final static String ROLE = "Role: ";
    final static String CLUSTER_ID = "Cluster ID: ";
    final static String HOST = "Host: ";
    final static String PORT = "Port: ";
    final static String NAME = "Name: ";
    final static String NUMBER = "Number: ";
    final static String NODE_STATUS = "Node status: ";
    final static String NODE_STATUS_CHANGED = "Node status changed: ";
    final static String NODE_STATE = "Nose state: ";
    final static String HOST_TYPE = "Host type: ";
    final static String TEAM_NAME = "Team name: ";
    final static String DOMAIN_NAME = "Domain name: ";
    final static String DATE = "Date: ";
    final static String STATUS = "Status: ";
    final static String CLUSTER_TYPE = "Cluster type: ";
    final static String HADOOP_APPLICATION_ID = "Hadoop application ID: ";
    final static String HADOOP_TYPE = "Hadoop type: ";
    final static String CPU = "CPU: ";
    final static String MEMORY = "Memory: ";
    final static String ABOUT = "About: ";
    final static String NUMBER_SIGN = "#";
    final static String TEAM_CAPS = "TEAM";
    final static String NAME_CAPS = "NAME";
    final static String STATUS_CAPS = "STATUS";
    final static String TITLE = "Title: ";
    final static String CATEGORY_TITLE = "Category title: ";
    final static String COMPANY_NAME = "Company name: ";
    final static String NOTES = "Nodes: ";
    final static String RELEASE_DATE = "Release date: ";
    final static String CLUSTER_APPLICATION_ID = "Cluster application ID: ";


    static void printWelcomeMessage(String clusterName) {
        CliHelper.printASCIIArt(CliHelper.PLANET_PATH, Color.ANSI_CYAN);
        System.out.println("\n\n\tWelcome to " + CliHelper.printColor(Color.ANSI_BLUE) + "ClusterGX"
                + CliHelper.printColor(Color.ANSI_RESET) + "!\n\n");
        LogHelper.print("Your cluster has been installed successfully and is ready for operation.\n");
        if (StringUtils.isNotBlank(clusterName))
            LogHelper.print("We have assigned you the cluster name: " + CliHelper.printColor(Color.ANSI_PURPLE)
                    + clusterName + CliHelper.printColor(Color.ANSI_RESET) + ".\n");
        try {
            // Sleep one second to show ASCII image
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // do nothing
        }
        LogHelper.print("If you want this computer to be a compute node please login and run "
                + CliHelper.printColor(Color.ANSI_GREEN) + "gex node install.\n"
                + CliHelper.printColor(Color.ANSI_RESET));
        LogHelper.print("Please send us your comments to " + CliHelper.printColor(Color.ANSI_GREEN)
                + "support@galacticexchange.io.\n" + CliHelper.printColor(Color.ANSI_RESET));
        LogHelper.print("You can find our documentation at " + CliHelper.printColor(Color.ANSI_GREEN)
                + "docs.galacticexchange.io\n" + CliHelper.printColor(Color.ANSI_RESET));
    }

    static void printNodeInstallationMessage(String nodeName) {
        System.out.println("\n\tThe name of this node is going to be " + CliHelper.printColor(Color.ANSI_PURPLE)
                + nodeName + CliHelper.printColor(Color.ANSI_RESET) + ".");
        System.out.println("\n\tWe have successfully installed and configured "
                + CliHelper.printColor(Color.ANSI_PURPLE) + nodeName + CliHelper.printColor(Color.ANSI_RESET) + ".");
        System.out.println("\n\tYour cluster node is starting, please wait.\n");
        System.out.println("\n\tTo check node state run \"gex node info\".\n");
        LogHelper.print("Please send us your comments to " + CliHelper.printColor(Color.ANSI_GREEN)
                + "support@galacticexchange.io.\n" + CliHelper.printColor(Color.ANSI_RESET));
        LogHelper.print("You can find our documentation at " + CliHelper.printColor(Color.ANSI_GREEN)
                + "docs.galacticexchange.io\n" + CliHelper.printColor(Color.ANSI_RESET));
    }

    static void printConfirmationCodeMessage(String phoneNum) {
        System.out.println("\n\tWe sent you an SMS message that includes your username and password to " + CliHelper.printColor(Color.ANSI_GREEN)
                + phoneNum + CliHelper.printColor(Color.ANSI_RESET) + ".");
    }

    //todo remove checks
    static void printMinPasswordLengthMessage() {
        CliHelper.printError(
                "Invalid password. Password must be at least " + ValidationHelper.MIN_PASSWORD_LENGTH + " characters");
    }

    static void printMaxPasswordLengthMessage() {
        CliHelper.printError("Invalid password. Password must be no more than " + ValidationHelper.MAX_PASSWORD_LENGTH
                + " characters");
    }
}
