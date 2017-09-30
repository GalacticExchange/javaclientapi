package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.EntityType;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.gex.core.DateConverter.TIMESTAMP_FORMAT;
import static java.time.ZoneOffset.UTC;

public class DownloadFile {
    private final static LogWrapper logger = LogWrapper.create(DownloadFile.class);
    private static final int CHUNK_SIZE = 51200;
    private int percentage;
    private boolean downloaded;
    private boolean downloading;
    private String url;
    private File file;
    private Exception exception;
    private long downloadSpeed;
    private boolean stopDownload;
    private long size;
    private long downloadedSize;
    private AfterDownloadAction afterDownloadAction;
    private String displayFileName;

    public DownloadFile(String url) {
        this.url = url;
        this.file = getTempFilePath("tmp");
    }

    public DownloadFile(String url, String extension) {
        this.url = url;
        this.file = getTempFilePath(extension);
    }

    public DownloadFile(String url, File file) {
        this.url = url;
        this.file = file;
    }

    public void downloadParallel() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkAvailability();
        String cmd = getDownloadCommand(10);
        ShellExecutor.executeCommand(ShellParameters.newBuilder(SystemUtils.IS_OS_WINDOWS ? Commands.cmd(cmd) :
                Commands.bash(cmd)).build());
    }

    public void downloadParallel(AtomicInteger progress) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        progress.set(0);
        checkAvailability();
        String cmd = getDownloadCommand(5);
        try {
            Process ps = ShellExecutor.getExecutionProcess(ShellParameters.newBuilder(SystemUtils.IS_OS_WINDOWS
                    ? Commands.cmd(cmd) : Commands.bash(cmd)).build());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String line;
            Pattern progressPattern = Pattern.compile("(?<=\\()\\d{1,3}(?=%\\))");
            while ((line = bufferedReader.readLine()) != null) {
                logger.logInfo(line, LogType.DOWNLOAD);
                Matcher matcher = progressPattern.matcher(line);
                if (matcher.find()) {
                    progress.set(Integer.valueOf(matcher.group(matcher.groupCount())));
                }
            }
            if (ps.waitFor() != 0) {
                throw new InterruptedException();
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.getExecutionCommandMessage(cmd), e, LogType.DOWNLOAD_ERROR);
        }
    }

    private void checkAvailability() throws GexException {
        try {
            URL website = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) website.openConnection();
            conn.setConnectTimeout(5000);
            if (conn.getContentLengthLong() < 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_LINK + url, LogType.DOWNLOAD_ERROR);
        }
    }

    private String getDownloadCommand(int printInterval) {
        if (PropertiesHelper.gexd.getProps().getNodeType() == EntityType.AWS) {
            return "wget -O " + getPath() + " " + url;
        } else {
            return (SystemUtils.IS_OS_MAC ? "\"/Library/Application Support/gex/aria2/bin/aria2c\"" : "aria2c") +
                    " -x16 --connect-timeout 5 --summary-interval " + printInterval +
                    " -d \"" + getFileDir() + "\" -o \"" + getFileName() + "\" " + url;
        }
    }

    public void download() {
        logger.trace("Entered " + LogHelper.getMethodName());
        new Thread(this::download0).start();
    }

    public void downloadSync() {
        logger.trace("Entered " + LogHelper.getMethodName());
        download0();
    }

    private void download0() {
        percentage = 0;
        downloaded = false;
        downloading = true;
        exception = null;
        downloadSpeed = 0;
        stopDownload = false;
        downloadedSize = 0;

        URLConnection conn;
        try {
            URL website = new URL(url);
            conn = website.openConnection();
            conn.setConnectTimeout(3000);
            try (FileOutputStream fos = new FileOutputStream(file);
                 InputStream rbc = conn.getInputStream()) {
                if (conn.getContentType().toLowerCase().contains("html")) {
                    throw logger.logAndReturnException(CoreMessages.INVALID_LINK + url, LogType.DOWNLOAD_ERROR);
                }
                if (conn.getContentLengthLong() > 0) {
                    size = conn.getContentLengthLong();
                }
                int curBytesTransfer;
                AtomicLong speedTimersBytesTransf = new AtomicLong();
                Timer speedTimer = initSpeedTimer(speedTimersBytesTransf);
                byte[] buffer = new byte[CHUNK_SIZE];
                while (!stopDownload && (curBytesTransfer = rbc.read(buffer)) > -1) {
                    fos.write(buffer, 0, curBytesTransfer);
                    downloadedSize += curBytesTransfer;
                    speedTimersBytesTransf.getAndAdd(curBytesTransfer);
                    if (size > 0) {
                        percentage = (int) ((double) downloadedSize / size * 100);
                    }
                }
                speedTimer.cancel();
            }
            if (!stopDownload) {
                if (!file.exists() || file.length() == 0) {
                    throw logger.logAndReturnException(CoreMessages.INVALID_LINK + url, LogType.DOWNLOAD_ERROR);
                } else {
                    downloaded = true;
                }
            } else if (file.exists() && !file.delete()) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.DELETE_FILE_ERROR,
                        file.getAbsolutePath()), LogType.DOWNLOAD_ERROR);
            }
        } catch (Exception e) {
            logger.logError(e, LogType.DOWNLOAD_ERROR);
            exception = e;
        } finally {
            downloading = false;
            if (afterDownloadAction != null) {
                afterDownloadAction.execute(this);
            }
        }
    }

    private Timer initSpeedTimer(AtomicLong receivedBytes) {
        receivedBytes.set(0);
        Timer timer = new Timer(true);
        long repeatTime = 2000;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                downloadSpeed = (long) (receivedBytes.doubleValue() * 1000 / repeatTime);
                receivedBytes.set(0);
            }
        };
        timer.scheduleAtFixedRate(timerTask, repeatTime, repeatTime);
        return timer;
    }

    public void deleteFile() {
        logger.trace("Entered " + LogHelper.getMethodName());
        FileUtils.deleteQuietly(file);
    }

    /*
    *
    * Automatically deletes file
     */
    public void stopDownload() {
        stopDownload = true;
    }

    private File getTempFilePath(String extension) {
        return new File(FileUtils.getTempDirectoryPath() + "/" + "gex_tmp_"
                + DateConverter.localDateTimeToString(LocalDateTime.now(), TIMESTAMP_FORMAT, UTC) + "." + extension);
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    private String getFileDir() {
        String path = file.getAbsolutePath();
        return path.substring(0, path.lastIndexOf(File.separator));
    }

    public String getFileName() {
        return file.getName();
    }

    public boolean isDownloading() {
        return downloading;
    }

    public Boolean isDownloaded() {
        return downloaded;
    }

    public Exception getException() {
        return exception;
    }

    public String getUrl() {
        return url;
    }

    public int getPercentage() {
        return percentage;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public long getSize() {
        return size;
    }

    public String getDownloadSpeedAsString() {
        return FileUtils.byteCountToDisplaySize(downloadSpeed) + "\\s";
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDisplayFileName() {
        return displayFileName;
    }

    public void setDisplayFileName(String displayFileName) {
        this.displayFileName = displayFileName;
    }

    public void setAfterDownloadAction(AfterDownloadAction afterDownloadAction) {
        this.afterDownloadAction = afterDownloadAction;
    }

    public interface AfterDownloadAction {
        void execute(DownloadFile downloadFile);
    }
}
