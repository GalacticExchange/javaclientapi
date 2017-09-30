package io.gex.core.shell;

import io.gex.core.BaseHelper;
import io.gex.core.log.LogType;

import java.util.List;
import java.util.Map;

public class ShellParameters {

    private List<String> cmd;
    private String dir;
    private Map<String, String> newEnv;
    private boolean printOutput = true;
    private boolean printCommand = true;
    private boolean logOutput = true;
    private boolean checkExitCode = true;
    private boolean redirectErrorStream = true;
    private String messageSuccess;
    private String messageError;
    private LogType typeSuccess;
    private LogType typeError;

    public List<String> getCmd() {
        return cmd;
    }

    public String getCmdString() {
        return BaseHelper.listToString(cmd);
    }

    public void setCmd(List<String> cmd) {
        this.cmd = cmd;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Map<String, String> getNewEnv() {
        return newEnv;
    }

    public void setNewEnv(Map<String, String> newEnv) {
        this.newEnv = newEnv;
    }

    public boolean isPrintOutput() {
        return printOutput;
    }

    public void setPrintOutput(boolean printOutput) {
        this.printOutput = printOutput;
    }

    public boolean isPrintCommand() {
        return printCommand;
    }

    public void setPrintCommand(boolean printCommand) {
        this.printCommand = printCommand;
    }

    public boolean isLogOutput() {
        return logOutput;
    }

    public void setLogOutput(boolean logOutput) {
        this.logOutput = logOutput;
    }

    public boolean isCheckExitCode() {
        return checkExitCode;
    }

    public void setCheckExitCode(boolean checkExitCode) {
        this.checkExitCode = checkExitCode;
    }

    public String getMessageSuccess() {
        return messageSuccess;
    }

    public void setMessageSuccess(String messageSuccess) {
        this.messageSuccess = messageSuccess;
    }

    public String getMessageError() {
        return messageError;
    }

    public void setMessageError(String messageError) {
        this.messageError = messageError;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    public void setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
    }

    public LogType getTypeSuccess() {
        return typeSuccess;
    }

    public void setTypeSuccess(LogType typeSuccess) {
        this.typeSuccess = typeSuccess;
    }

    public LogType getTypeError() {
        return typeError;
    }

    public void setTypeError(LogType typeError) {
        this.typeError = typeError;
    }

    public static ShellBuilder newBuilder(List<String> cmd) {
        return new ShellBuilder(cmd);
    }

    public static class ShellBuilder {
        private ShellParameters parameters = new ShellParameters();

        ShellBuilder(List<String> cmd) {
            parameters.setCmd(cmd);
        }

        public ShellBuilder setDir(String dir) {
            parameters.setDir(dir);
            return this;
        }

        public ShellBuilder setNewEnv(Map<String, String> newEnv) {
            parameters.setNewEnv(newEnv);
            return this;
        }

        public ShellBuilder setPrintOutput(boolean printOutput) {
            parameters.setPrintOutput(printOutput);
            return this;
        }

        public ShellBuilder setPrintCommand(boolean printCommand) {
            parameters.setPrintCommand(printCommand);
            return this;
        }

        public ShellBuilder setLogOutput(boolean logOutput) {
            parameters.setLogOutput(logOutput);
            return this;
        }

        public ShellBuilder setCheckExitCode(boolean checkExitCode) {
            parameters.setCheckExitCode(checkExitCode);
            return this;
        }

        public ShellBuilder setMessageSuccess(String messageSuccess) {
            parameters.setMessageSuccess(messageSuccess);
            return this;
        }

        public ShellBuilder setMessageError(String messageError) {
            parameters.setMessageError(messageError);
            return this;
        }

        public ShellBuilder setRedirectErrorStream(boolean redirectErrorStream) {
            parameters.setRedirectErrorStream(redirectErrorStream);
            return this;
        }

        public ShellBuilder setTypeSuccess(LogType type) {
            parameters.setTypeSuccess(type);
            return this;
        }

        public ShellBuilder setTypeError(LogType type) {
            parameters.setTypeError(type);
            return this;
        }

        public ShellBuilder setType(LogType typeSuccess, LogType typeError) {
            parameters.setTypeSuccess(typeSuccess);
            parameters.setTypeError(typeError);
            return this;
        }

        public ShellParameters build() {
            return parameters;
        }
    }
}
