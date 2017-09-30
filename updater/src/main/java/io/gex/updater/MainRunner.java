package io.gex.updater;

import io.gex.core.CoreMessages;
import io.gex.core.UpdateHelper;
import io.gex.core.WindowsInstallInfoHelper;
import io.gex.core.exception.ExceptionHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogSource;
import io.gex.core.log.LogType;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class MainRunner extends Application {

    private final static Logger logger = LogManager.getLogger(MainRunner.class);

    private static final String DEFAULT_BACKGROUND_COLOR = "0xF2F1F0";

    private TextFlow messageTextFlow;

    public static void main(String[] arguments) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (arguments.length != 2) {
            System.out.println(UpdaterMessages.INVALID_PARAMETERS);
            logger.error(UpdaterMessages.INVALID_PARAMETERS + ": " + Arrays.toString(arguments));
            System.exit(1);
        }
        UpdaterUtils.init();
        if (arguments[0].toLowerCase().equals("cli")) {
            logger.info(UpdaterMessages.CLI_MODE);
            try {
                UpdateHelper.OSCheck();
                System.out.println(UpdaterMessages.START_UPDATING);
                UpdaterUtils.update(arguments[1]);
            } catch (GexException e) {
                logger.error(e.getMessage());
                System.out.println(e.getMessage());
                System.exit(1);
            }
            System.out.println(UpdaterMessages.FINISH_UPDATING);
        } else if (arguments[0].toLowerCase().equals("ui")) {
            logger.info(UpdaterMessages.UI_MODE);
            launch(arguments);
        } else {
            logger.error(CoreMessages.UNSUPPORTED_APPLICATION_MODE);
            System.out.println(CoreMessages.UNSUPPORTED_APPLICATION_MODE);
            System.exit(1);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ClusterGX Updater");
        primaryStage.getIcons().add(new Image(UpdaterUtils.getResourceAsStream("gexlogo.png")));
        primaryStage.setResizable(false);

        VBox root = new VBox(30);
        root.setBackground(new Background(new BackgroundFill(Color.web(DEFAULT_BACKGROUND_COLOR),
                CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        createElements(root);

        Scene scene = new Scene(root, 600, 350);
        primaryStage.setScene(scene);
        primaryStage.show();

        executeUpdate(primaryStage);
    }

    private void executeUpdate(Stage primaryStage) {
        MainRunner self = this;

        Task<String> updateTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                UpdateHelper.OSCheck();
                logger.info("Started update");
                UpdaterUtils.update(self.getParameters().getUnnamed().get(1));
                logger.info("Finished update");
                return StringUtils.EMPTY;
            }
        };
        new Thread(updateTask).start();

        primaryStage.setOnCloseRequest(we -> {
            if (updateTask.isRunning()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(UpdaterUtils.getResourceAsStream("gexlogo.png")));
                alert.setTitle("Warning");
                alert.setHeaderText("Can not close updater");
                alert.setContentText("You can not close window while update is running.");
                alert.show();
                we.consume();
            }
        });

        updateTask.setOnFailed(event -> {
            Text linkToLogs = createMessageText("logs");
            linkToLogs.setUnderline(true);
            setMessage(createMessageText("Update failed. Please see "), linkToLogs, createMessageText("."));

            messageTextFlow.setOnMouseClicked(event1 -> {
                if (event1.getButton().equals(MouseButton.PRIMARY) && event1.getTarget() instanceof Text
                        && ((Text) event1.getTarget()).isUnderline()) {
                    try {
                        Desktop.getDesktop().open(Paths.get(UpdaterUtils.getLogFilePath()).toFile());
                    } catch (IOException e) {
                        setMessage(createMessageText(e.getMessage()));
                        ExceptionHelper.logAndReturnException(logger,e, LogSource.APP, LogType.FILE_ERROR);
                    }
                }
            });
        });
        updateTask.setOnSucceeded(event -> {
            try {
                if (SystemUtils.IS_OS_WINDOWS) {
                    ShellExecutor.getExecutionProcess(Commands.cmd("START \"\" \"" +
                            WindowsInstallInfoHelper.getInstallationPath() + "\\usr\\lib\\gex\\ui\\clustergx.exe\""),
                            LogSource.APP);
                } else if (SystemUtils.IS_OS_MAC) {
                    ShellExecutor.getExecutionProcess(Commands.osascript("open app \"ClusterGX\""),
                            LogSource.APP);
                }
            } catch (GexException e) {
                logger.error(e.getMessage());
            }
            Platform.exit();
        });
    }

    private void createElements(VBox root) {
        Image logo = new Image(UpdaterUtils.getResourceAsStream("gexlogo.png"));
        ImageView logoView = new ImageView();
        logoView.setImage(logo);
        logoView.setFitHeight(128);
        logoView.setFitWidth(128);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        logoView.setCache(true);

        root.getChildren().add(logoView);

        Label updatingLabel = new Label("Updating ClusterGX");
        updatingLabel.setFont(Font.font(18));
        root.getChildren().add(updatingLabel);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressBar.setPrefWidth(450);
        progressBar.setPrefHeight(30);
        root.getChildren().add(progressBar);

        messageTextFlow = new TextFlow();
        messageTextFlow.setTextAlignment(TextAlignment.CENTER);
        root.getChildren().add(messageTextFlow);
    }

    private void setMessage(Text... texts) {
        messageTextFlow.getChildren().clear();
        messageTextFlow.getChildren().addAll(texts);
    }

    private Text createMessageText(String text) {
        Text textElement = new Text(text);
        textElement.setFont(Font.font(16));
        textElement.setFill(Color.web("#DB6352"));
        return textElement;
    }

}
