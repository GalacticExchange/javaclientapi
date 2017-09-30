package io.gex.agent;


public class GexdMessages {

    public final static String SHUT_DOWN_ERROR = "Failed to shut down the machine.";
    public final static String RESTART_ERROR = "Failed to restart the machine.";
    public final static String RESTART = "Machine restarted.";
    public final static String WEB_SERVER_DOWN = "Local web server in down.";
    public final static String PORT_SEARCH_ERROR = "Failed to find a free port for web server.";
    public final static String WEB_SERVER_IP_SEARCH_ERROR = "Failed to get ip address for web server.";
    public final static String SERVER_MODE = "Node has started in the server mode.";
    public final static String JOINED_NODE = "Starting node ...";
    public final static String STARTING_NODE = "Node installation is not finished. Starting node ...";
    public final static String RESTARTING_NODE = "Restarting node ...";
    public final static String STOPPING_NODE = "Stopping node ...";
    public final static String STOPPED_NODE = "Shutdown node.";
    public final static String CONFIG_FINISHED = "Initial config is finished.";
    public final static String CLOSING_CHANNEL = "Closing the RabbitMQ channel ...";
    public final static String CLOSING_CONNECTION = "Closing the RabbitMQ connection ... ";
    public final static String CLOSING_ERROR = "Failed to close RabbitMQ channel and connection.";
    public final static String WAITING = "Waiting ...";
    public final static String RECEIVED = "Received: ";
    public final static String SENT = "Sent: ";
    public final static String WINDOWS_FIREWALL_RULE_NOT_FOUND = "Unable to fins firewall rule.";
    public final static String EXCHANGE_CONNECTION = "Trying to connect to the exchange ... Retrying.";
    public final static String FORBIDDEN = "Forbidden operation";
    public final static String FORBIDDEN_LOG = "Forbidden. Log file is available only for the same team.";
    public final static String FORBIDDEN_TOKEN = "Forbidden. No token is granted.";
    public final static String RESULT = "result";
    public final static String START_SENDING_LOG = "Start sending log file.";
    public final static String START_SENDING_BOX = "Start sending log box";
    public final static String INVALID_REQUEST = "Invalid request.";
    public final static String APPLICATION_NAME = "applicationName";
    public final static String APPLICATION_ID = "applicationID";
    public final static String CONTAINER_NAME = "containerName";
    public final static String CONTAINER_ID = "containerID";
    public final static String EXTERNAL = "external";
    public final static String DELAY = "Delay time for send status task is ";
    public final static String INIT_RETRY = "Retrying the initialization.";
    public final static String RABBIT_QUEUE = "Rabbit queue: ";
    public final static String RABBIT_CONNECTION_OPENED = "Rabbit connection opened.";
    public final static String DELETING_WINDOWS_RULE = "Deleting rule: ";
    public final static String DELETING_FILE = "Deleting file: ";
    public final static String APPLICATION_DOWNLOADED = "Application downloaded.";
    public final static String QUEUE_DISCONNECTED = "Disconnected from queue.";
    public final static String NODE_ID_ERROR = "Failed to get nodeID.";
    public final static String NODES_REMOTE_LOGS = "Failed to get remote install/uninstall logs.";
    public final static String NODE_SETUP_ERROR = "Failed to setup node.";
    public final static String NODE_INSTALL_ERROR = "Failed to install node.";
    public final static String NODE_UNINSTALL_ERROR = "Failed to uninstall node.";
    public final static String NODE_UNINSTALL_FORCE_ERROR = "Failed to force uninstall node.";
    public final static String SEND_BOX_ERROR = "Failed to send box.";
    public final static String SEND_LOG_ERROR = "Failed to send agent log file.";
    public final static String BOX_NOT_FOUND = "Box not found.";
    public final static String LOG_NOT_FOUND = "Log file not found.";
    public final static String NODE_LOCAL_ERROR = "Failed to get local node info.";
    public final static String NODE_INST_REMOTE_ERROR = "Failed to install node remotely.";
    public final static String NODE_UNINST_REMOTE_ERROR = "Failed to uninstall node remotely.";
    public final static String APP_LOCAL_ERROR = "Failed to get local app info.";
    public final static String GEXD_STATUS = "Gexd status: ";
    public final static String INVALID_NODE_STATE = "Node is in invalid 'stopped' state.";
    //kafka
    public final static String LOGGER_PACKAGE = "io.gex";
    public final static String KAFKA_APPENDER = "Kafka";
    public final static String NEW_KAFKA_APPENDER = "NewKafka";
    public final static String ASYNC_KAFKA_APPENDER = "AsyncKafka";
    public final static String NEW_ASYNC_KAFKA_APPENDER = "NewAsyncKafka";
    public final static String ROLLING_FILE = "RollingFile";
    public final static String KAFKA_LOGGER = "Failed to change the Kafka topic.";
    public final static String KAFKA_BOOTSTRAP_SERVERS = "bootstrap.servers";

    //vagrant
    public final static String NODE_START = "node start command completed.";
    public final static String NODE_START_ERROR = "node start command execution failed.";
    public final static String NODE_RESTART = "node restart command completed.";

    public final static String VAGRANT_UP = "\"vagrant up\" command completed.";
    public final static String VAGRANT_RELOAD = "\"vagrant reload\" command completed.";
    public final static String VAGRANT_HALT = "\"vagrant halt\" command completed.";
    public final static String VAGRANT_HALT_ERROR = "\"vagrant halt\" command failed.";
    public final static String VAGRANT_DESTROY_ERROR = "\"vagrant destroy\" command failed.";
    public final static String VAGRANT_RELOAD_UP_ERROR = "\"vagrant reload\" (up part) command failed.";
    public final static String VAGRANT_RELOAD_UP = "\"vagrant reload\" (up part) completed.";
    public final static String VAGRANT_RELOAD_HALT_ERROR = "\"vagrant reload\" (halt part) execution failed.";
    public final static String VAGRANT_RELOAD_HALT = "\"vagrant reload\" (halt part) completed.";
    public final static String VAGRANT_UP_ERROR = "\"vagrant up\" execution failed.";
    public final static String VAGRANT_DESTROY = "\"vagrant destroy\" completed.";
    public final static String STEP_1 = "Step 1: Stopping box ...";
    public final static String STEP_2 = "Step 2: Destroying box ...";
    public final static String STEP_3 = "Step 3: Removing configuration files ...";
    public final static String STEP_1_COMPLETED = "Step 1 completed.";
    public final static String STEP_2_COMPLETED = "Step 2 completed.";
    public final static String STEP_3_COMPLETED = "Step 3 completed.";
    public final static String STEP_1_WARNING = "WARNING! Step 1 was finished with error.";
    public final static String STEP_2_WARNING = "WARNING! Step 2 was finished with error.";
    public final static String VAGRANT_PROVISION_INSTALL = "\"vagrant install container\" command completed.";
    public final static String VAGRANT_PROVISION_INSTALL_ERROR = "\"vagrant install container\" execution failed.";
    public final static String VAGRANT_PROVISION_UNINSTALL = "\"vagrant uninstall container\" command completed.";
    public final static String VAGRANT_PROVISION_UNINSTALL_ERROR = "\"vagrant uninstall container\" execution failed.";
    public final static String VAGRANT_PROVISION_RUN = "\"vagrant run container\" command completed.";
    public final static String VAGRANT_PROVISION_RUN_ERROR = "\"vagrant run container\" execution failed.";
    public final static String VAGRANT_CONTAINER_START = "\"vagrant start container\" command completed.";
    public final static String VAGRANT_CONTAINER_START_ERROR = "\"vagrant start container\" execution failed.";
    public final static String VAGRANT_CONTAINER_STOP = "\"vagrant stop container\" command completed.";
    public final static String VAGRANT_CONTAINER_STOP_ERROR = "\"vagrant stop container\" execution failed.";
    public final static String VAGRANT_CONTAINER_RESTART = "\"vagrant restart container\" command completed.";
    public final static String VAGRANT_CONTAINER_RESTART_ERROR = "\"vagrant restart container\" execution failed.";
    public final static String CONTAINER_START = "\"start container\" command completed.";
    public final static String CONTAINER_START_ERROR = "\"start container\" execution failed.";
    public final static String CONTAINER_STOP = "\"stop container\" command completed.";
    public final static String CONTAINER_STOP_ERROR = "\"stop container\" execution failed.";
    public final static String CONTAINER_RESTART = "\"restart container\" command completed.";
    public final static String CONTAINER_RESTART_ERROR = "\"restart container\" execution failed.";
    public final static String INSTALL_CONTAINER = "\"install_container\" execution failed.";
    public final static String RUN_CONTAINER = "\"run_container\" execution failed.";
    public final static String REMOVE_CONTAINER = "\"remove_container\" execution failed.";
    public final static String INSTALL_CONTAINER_ERROR = "\"install_container\" execution failed.";
    public final static String RUN_CONTAINER_ERROR = "\"run_container\" execution failed.";
    public final static String REMOVE_CONTAINER_ERROR = "\"remove_container\" execution failed.";
    public final static String REMOVE_VAGRANT_LOCK = "Vagrant box is locked. Trying to remove lock and restart command ...";
    public final static String VAGRANT_SSH_CONNECTION = "The SSH connection was unexpectedly closed by the remote end.";
    public final static String VAGRANT_LOCKED = "Vagrant can't use the requested machine because it is locked!";
    public final static String VAGRANT_STATUS = "\"vagrant status\" command completed.";
    public final static String VAGRANT_STATUS_ERROR = "\"vagrant status\" execution failed.";
    public final static String VAGRANT_INDEX_CORRUPTED = "running Vagrant environments has become corrupt";
}
