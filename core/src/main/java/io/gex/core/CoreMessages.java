package io.gex.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class CoreMessages {

    // common
    public static final String DATA_TEMPLATE = "{data}";
    public final static String AUTHENTICATION_ERROR = "Failed to authenticate.";
    public final static String NO_INTERNET_CONNECTION = "No internet connection.";
    public final static String NO_API_CONNECTION = "No connection to server. Try again later.";
    public final static String HARDWARE_INFO_ERROR = "Failed to get the hardware info.";
    public final static String EXECUTE_COMMAND = "Executing command: ";
    public final static String IN_DIR = "In directory: ";
    public final static String RE_AUTHENTICATE = "Authentication token expired. Please login again.";
    public final static String UNSUPPORTED_OS = "Unsupported Operating System.";
    public final static String SHELL_ERROR = "Shell command execution failed.";
    public final static String EMPTY_COMMAND = "Command can not be empty";
    public final static String FILE_NOT_FOUND = "File not found: " + DATA_TEMPLATE + ".";
    public final static String DELETE_FILE_ERROR = "Can not delete file " + DATA_TEMPLATE + ".";
    public final static String RECONNECTING = "Unable to connect to server. Retrying... Attempt: ";
    public final static String SERVER_CONNECTION_ERROR = "Server connection failed.";
    public final static String SERVER_COMMAND_ERROR = "Failed to execute server command.";
    public final static String SERVER_RESPONSE_ERROR = "Server response parsing failed.";
    public final static String SERVER_ERROR = "Server Error";
    public final static String WINDOWS_INSTALL_INFO_FILE_ERROR = "Invalid Windows \"install.info\" file.";
    public final static String DOWNLOAD_FILE_ERROR = "Failed to download file.";
    public final static String DOWNLOAD_FILE_WITH_NAME_ERROR = "Failed to download file: ";
    public final static String CAN_NOT_CONNECT_TO_MESSAGE_BROKER = "Can not connect to message broker.";
    public final static String HOST_TYPE_ERROR = "Failed to get host type from configuration files.";
    public final static String GEXD_CHECK_ERROR = "Failed to check that gexd is running.";
    public final static String GEXD_WEB_SERVER_IS_NOT_RUNNING = "Gexd web server is not running.";
    public final static String GEXD_IS_NOT_RUNNING = "Gexd service is not running. Start it to continue the installation.";
    public final static String GEXD_IS_NOT_RUNNING_LINUX =
            "Gexd service is not running. To start it execute: sudo supervisorctl start gexd";
    public final static String OS_INFO_ERROR = "Failed to get Operation System info.";
    public final static String ACCOUNT_INFO_ERROR = "Failed to get user profile info.";
    public final static String COMMAND_DEPRECATED_ON_SERVICE = "Failed to execute command in service mode.";
    public final static String WARNING = "WARNING: ";
    public final static String INVALID_PARAMETER = "Invalid parameter: ";
    public final static String GET_LOG_FILE_ERROR = "Unable to download log file. " +
            "You can download log file only if you are in local network with this node. " +
            "Probably this machine has a firewall.";
    public final static String DOWNLOAD_UNINSTALLER_ERROR = "Failed to download remote uninstall script.";
    public final static String DOWNLOAD_INSTALLER_ERROR = "Failed to download remote install script.";
    public final static String NETWORK_CHECK = "Your machine should be connected to the network.";
    public final static String BOX_DOWNLOADING_START = "Start box downloading.";
    public final static String APPLICATION_DOWNLOADING_START = "Start application downloading.";
    public final static String READ_WEB_SERVER_PORT_ERROR = "Failed to read web server port property. Using default value.";
    public static final String BOX_IS_CORRUPTED = "Downloaded node box is corrupted. Please reinstall node.";
    public static final String APP_BOX_IS_CORRUPTED = "Downloaded application box is corrupted. Please reinstall application.";
    public final static String DOWNLOADING = "Downloading ";
    public final static String AWS_INSTANCE_ID_ERROR = "Failed to read AWS instanceID.";
    public final static String LINUX_UPDATE = "For updating ClusterGX on linux use commands:\n\tsudo apt-get update\n\tsudo apt-get install gex";
    public final static String SERVER_PROPERTY_ERROR = "Failed to get property from server.";
    public final static String LOCAL_NODES = "Local nodes: ";
    public final static String TRYING_TO_DOWNLOAD_FROM = "Try to download from: ";
    public final static String BOX_DOWNLOADED = "Box downloaded.";
    public final static String NON_ZERO = "Operation finished with non-zero exit code.";

    // Empty
    public final static String EMPTY_INSTANCE_ID = "Instance ID module cannot be empty.";
    public final static String EMPTY_MODULE = "Downloading module cannot be empty.";
    public final static String EMPTY_FILE_NAME = "File name cannot be empty.";
    public final static String EMPTY_VERIFICATION_TOKEN = "Verification token cannot be empty.";
    public final static String EMPTY_PHONE_NUMBER = "Phone number cannot be empty.";
    public final static String EMPTY_CLUSTER_CREATE = "Cluster create parameters cannot be empty.";
    public final static String EMPTY_CLUSTER_TYPE = "Cluster type cannot be empty.";
    public final static String EMPTY_INVITATION_ID = "Invitation id cannot be empty.";
    public final static String EMPTY_USERNAME = "Username cannot be empty.";
    public final static String EMPTY_USER = "User cannot be empty.";
    public final static String EMPTY_ROLE = "Role cannot be empty.";
    public final static String EMPTY_EMAIL = "Email cannot be empty.";
    public final static String EMPTY_PASSWORD = "Password cannot be empty.";
    public final static String EMPTY_NEW_PASSWORD = "New password cannot be empty.";
    public final static String EMPTY_OLD_PASSWORD = "Old password cannot be empty.";
    public final static String EMPTY_ACTION = "Action cannot be empty.";
    public final static String EMPTY_APP_ID = "Application id cannot be empty.";
    public final static String EMPTY_NODE_NAME = "Node name cannot be empty.";
    public final static String EMPTY_NODE_AGENT_TOKEN = "Node agent token cannot be empty.";
    public final static String EMPTY_NODE_ID = "Node id cannot be empty.";
    public final static String EMPTY_CLUSTER_ID = "Cluster id cannot be empty.";
    public final static String EMPTY_HADOOP_APP = "HadoopApp property cannot be empty.";
    public final static String EMPTY_TEAM = "Team info cannot be empty.";
    public final static String EMPTY_TEAM_NAME = "Team name cannot be empty.";
    public final static String EMPTY_FIRST_NAME = "First name cannot be empty.";
    public final static String EMPTY_LAST_NAME = "Last name cannot be empty.";
    public final static String EMPTY_SOURCE_FOLDER = "Source folder cannot be empty.";
    public final static String EMPTY_DESTINATION_FOLDER = "Destination folder cannot be empty.";
    public final static String EMPTY_IP = "IP cannot be empty.";
    public final static String EMPTY_OBJECT = " Empty object.";
    public final static String EMPTY_UPDATER = "Updater path cannot be empty.";
    public final static String EMPTY_DISTRIBUTION = "Distribution path cannot be empty.";
    public final static String EMPTY_TOKEN = "Token cannot be empty.";
    public final static String EMPTY_HADOOP_TYPE = "Hadoop type cannot be empty.";
    public final static String EMPTY_APPLICATION_ID = "Application ID cannot be empty.";
    public final static String EMPTY_EXTERNAL = "Application external identification cannot be empty.";
    public final static String EMPTY_APPLICATION_NAME = "Application name cannot be empty.";
    public final static String EMPTY_CONTAINER_NAME = "Container name cannot be empty.";
    public final static String EMPTY_CONTAINER_ID = "Container ID cannot be empty.";
    public final static String EMPTY_MESSAGE = "Message cannot be empty.";

    //invalid
    public final static String INVALID_APPLICATION_MODE = "Invalid application mode.";
    public final static String INVALID_PORT = "Invalid port.";
    public final static String INVALID_REQUEST = "Invalid request.";
    public final static String INVALID_DESTINATION = "Invalid destination folder.";
    public final static String INVALID_LINK = "Invalid download url: ";
    public final static String INVALID_QUERY = "Failed to send request to server. Invalid query parameters.";
    public final static String INVALID_KEY_PARAMETERS = "Invalid key server response parameters: ";
    public final static String EMPTY_PARAMETERS_FOR_ENTITY = "Empty parameters for entity " + DATA_TEMPLATE + ": ";

    public final static String TEAM_NAME_REGEX_ERROR = "Team name can only contain lowercase alphanumeric characters or single hyphens, " +
            "and cannot begin or end with a hyphen.";
    public final static String USERNAME_REGEX_ERROR = "Username can only contain lowercase alphanumeric characters or single hyphens, " +
            "and cannot begin or end with a hyphen.";
    public final static String SAME_PASSWORDS = "Old password and new password should not be the same.";

    public final static String APPLICATION_INSTALLED = "Application " + DATA_TEMPLATE + " has been installed.";
    public final static String APPLICATION_INSTALL_ERROR = "Failed to install " + DATA_TEMPLATE + " application.";
    public final static String APPLICATION_UNINSTALLED = "Application " + DATA_TEMPLATE + " has been uninstalled.";
    public final static String APPLICATION_UNINSTALL_ERROR = "Failed to uninstall " + DATA_TEMPLATE + " application.";
    public final static String APPLICATION_PRESENT = "Application " + DATA_TEMPLATE + " is already present.";

    public final static String NODE_INSTALLED = "Cluster node has been installed.";
    public final static String NODE_UNINSTALLED = "Cluster node has been uninstalled.";
    public final static String NODE_INSTALL_ERROR = "Failed to install the cluster node.";
    public final static String NODE_REMOTE_INSTALL_ERROR = "Failed to install the remote cluster node.";
    public final static String NODE_UNINSTALL_ERROR = "Failed to uninstall the cluster node.";
    public final static String NODE_UNINSTALLING = "Start node uninstalling.";
    public final static String NO_FILES_TO_DOWNLOAD = "There are no files available for downloading.";
    public final static String NODE_EXIST = "You already have a cluster node on your machine." +
            "\n\tTo install new node uninstall existing node first.";
    public final static String NODE_NOT_EXIST = "There is no cluster node on your machine.";
    public final static String APPLICATION_EXIST = "You already have " + DATA_TEMPLATE +
            " application installed on your machine.\n\tPlease uninstall existing application first.";
    public final static String APPLICATION_NOT_EXIST = WordUtils.capitalize(DATA_TEMPLATE) +
            " application is not installed.";
    public final static String CONFIG_FILES_REMOVED = "Configuration files removed.";
    public final static String CONFIG_FILES_REMOVE_ERROR = "Configuration files removing failed.";
    public final static String NODE_NOT_RUNNING = "Node is not running";
    public final static String NODE_NOT_POWER_OFF = "Node is not powered off";

    //update
    public final static String VERSION_COMPARE_ERROR = "Failed to compare versions.";
    public final static String UPLOADS_ERROR = "Failed to get uploads.";
    public final static String JAVA_COPY_ERROR = "Failed to copy java.";
    public final static String UPDATER_START_ERROR = "Unable to start updater.";
    public final static String READING_FROM_RESOURCE_ERROR = "Failed to read from: " + DATA_TEMPLATE;

    // properties
    public final static String NO_PROPERTIES_DIRECTORY = "Cannot locate the user properties directory.";
    public final static String NO_SERVICE_PROPERTIES_DIRECTORY = "Cannot create \"C:\\ProgramData\\.gex\" directory.";
    public final static String NO_CONFIG_PROPERTIES = "Can not locate the configuration file.";
    public final static String USER_PROPERTIES_ERROR = "Invalid user properties file.";
    public final static String NODE_PROPERTIES_ERROR = "Invalid node properties file.";
    public final static String GEXD_PROPERTIES_ERROR = "Invalid gexd properties file.";
    public final static String CONFIG_PROPERTIES_ERROR = "Invalid configuration file: ";
    public final static String SERVICE_UPDATE_ERROR = "Permissions denied. Only service can update ";
    public final static String USER_UPDATE_ERROR = "Permissions denied. Only user can update ";

    // lock
    public final static String LOCK = "Locked file ";
    public final static String LOCK_RELEASE = "Unlocked file ";
    public final static String CREATE_LOCK_ERROR = "Failed to make file lock.";
    public final static String FILE_ALREADY_LOCKED = "Another process is currently working with the node.";

    // parsing
    public final static String CLUSTER_PARSING_ERROR = "Cluster parsing failed.";
    public final static String NODE_INST_CONF_PARSING_ERROR = "Node install configuration parsing failed.";
    public final static String NODE_UNINST_CONF_PARSING_ERROR = "Node uninstall configuration parsing failed.";
    public final static String CLUSTER_OPTIONS_PARSING_ERROR = "Cluster options parsing failed.";
    public final static String FILE_PARSING_ERROR = "File parsing failed.";
    public final static String INVITATION_PARSING_ERROR = "Invitation parsing failed.";
    public final static String NODE_PARSING_ERROR = "Node parsing failed.";
    public final static String NODE_COUNTERS_PARSING_ERROR = "Node counters parsing failed.";
    public final static String SERVICE_PARSING_ERROR = "Service parsing failed.";
    public final static String TEAM_PARSING_ERROR = "Team parsing failed.";
    public final static String USER_PARSING_ERROR = "User parsing failed.";
    public final static String AGENT_PARSING_ERROR = "Agent parsing failed.";
    public final static String APPLICATION_PARSING_ERROR = "Application parsing failed.";
    public final static String CONTAINER_PARSING_ERROR = "Container parsing failed.";
    public final static String SOCKS_PROXY_PARSING_ERROR = "Socks proxy parsing failed.";

    // ssh
    public final static String CLOSE_CONNECTION = "SSH connection closed.";
    public final static String CHANNEL_ERROR = "Failed to get connection channel.";
    public final static String ERROR_RETURN_CODE = "Command returns a non-zero exit code.";

    //vagrant
    public final static String INSTALL_VAGRANT_ERROR = "Failed to install Vagrant.";
    public final static String VAGRANT_BOX_PRESENT =
            "Vagrant box \"gex\\client\" is present in VAGRANT_HOME directory. Please remove it and try again. Box path: ";
    public final static String OLD_BOXES_DELETE_GENERAL_ERROR = "Unable to remove old boxes from " +
            CoreMessages.DATA_TEMPLATE + " directory.";
    public final static String OLD_BOXES_DELETE_ERROR = "Unable to remove old boxes:";
    public final static String REACH_DOWNLOAD_BOX_URL_ERROR = "Failed to reach download box URL: ";
    public final static String BOX_VERSION_ERROR = "Failed to get box version.";
    public final static String BOX_CHECKSUM_ERROR = "Failed to get box checksum.";
    public final static String BOX_CHECKSUM_PARSE_ERROR = "Failed to parse box checksum: ";
    public final static String BOX_INFO_ERROR = "Failed to get box info.";
    public final static String BOX_PRESENT = "Box file has already been downloaded.";
    public final static String NO_VAGRANT_HOME = "VAGRANT_HOME is not defined. Using default value.";
    public final static String VAGRANT_VERSION_PARSE_ERROR = "Cannot parse vagrant version from: ";
    public final static String VAGRANT_NOT_INSTALLED = "Vagrant is not installed";

    //virtual box
    public final static String GET_DEFAULT_MACHINE_FOLDER_ERROR = "Failed to get VirtualBox default machine folder property.";
    public final static String VIRTUAL_BOX_NOT_INSTALLED = "VirtualBox is not installed.";
    public final static String INSTALL_VIRTUAL_BOX_ERROR = "Failed to install VirtualBox.";
    public final static String RUNNING_VM = "The installer has detected running virtual machines. " +
            "Please shut down all running VirtualBox machines and then restart the installation.";
    public final static String MACHINE_FOLDER_ERROR = "Failed to set VirtualBox machine folder property.";
    public final static String SET_VIRTUAL_BOX_HOME_ERROR = "Failed to set VirtualBox box home.";
    public final static String GET_VIRTUAL_BOX_HOME_ERROR = "Failed to get VirtualBox box home.";
    public final static String VIRTUAL_BOX_NAME_ERROR = "Failed to get VirtualBox box name.";
    public final static String VIRTUAL_BOX_LOG_FILE_ERROR = "Failed to get VirtualBox log file: ";
    public final static String NO_VIRTUAL_BOX_LOG_FILE = "Cannot locate VirtualBox log file.";
    public final static String VIRTUAL_BOX_HOME = "Setting VirtualBox home ...";
    public final static String DKMS_ERROR = "The character device /dev/vboxdrv does not exist.\n" +
            "     Please install the virtualbox-dkms package and the appropriate\n" +
            "     headers, most likely linux-headers-generic.\n\n" +
            "     You will not be able to start VMs until this problem is fixed.";

    //check virtualization
    public final static String VIRTUALIZATION_NOT_ENABLED = "Virtualization not enabled on you computer." +
            "\nEnter your BIOS setup and enable Virtualization Technology (VT) and then hard poweroff/poweron your system.";
    public final static String VIRTUALIZATION_NOT_SUPPORTED = "Your CPU does not support virtualization.";
    public final static String VIRTUALIZATION_UNEXP_EXCEPTION = "Unexpected exception when check virtualization";
    public final static String HYPERV_ENABLED = "Hyper-V is enabled. To disable, go to \"Programs and Features\", " +
            "click on \"Turn Windows features on or off\" and uncheck the box next to \"Hyper-V\" and restart your computer.";

    // request and response
    public final static String HEADER = "Headers: ";
    public final static String HEADER_EMPTY = "Header is empty.";
    public final static String QUERY = "Query parameters: ";
    public final static String QUERY_EMPTY = "Query is empty.";
    public final static String BODY = "Body: ";
    public final static String BODY_EMPTY = "Body is empty.";
    public final static String VERSION = "Version: ";
    public final static String TOKEN = "Token: ";
    public final static String NODE_AGENT_TOKEN = "Node Agent Token: ";
    public final static String REQUEST = "\n\tHTTP Request: ";
    public final static String RESPONSE = "\n\tHTTP Response: ";
    public final static String RETURN_CODE = "Return code: ";
    public final static String SERVER_RESPONSE_TIME = "Server response time = ";
    public final static String UNEXPECTED_AUTH_EXCEPTION = "Unexpected authentication exception.";

    public static String getExecutionCommandMessage(String command) {
        return "Shell command \"" + command + "\" execution failed";
    }

    public static String getConnectionErrorMessage(String domainName) {
        return StringUtils.isNotBlank(domainName) ? "Failed to connect to " + domainName + "." : "Connection failed.";
    }

    public static String replaceTemplate(String text, String replacement) {
        return StringUtils.replace(text, DATA_TEMPLATE, replacement);
    }
}
