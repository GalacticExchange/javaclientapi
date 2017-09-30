package io.gex.core.uploader;

import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Paths;

public class UpdateUploader extends Uploader {

    private final static LogWrapper logger = LogWrapper.create(UpdateUploader.class);

    private final static String UPDATER_EXTENSION = "jar";
    private final static String WINDOWS_EXTENSION = "exe";
    private final static String MAC_EXTENSION = "dmg";
    private final static String WINDOWS = "windows";
    private final static String MAC = "mac";
    private String extension;
    private String dir;

    public UpdateUploader() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_WINDOWS) {
            extension = WINDOWS_EXTENSION;
            dir = WINDOWS;
        } else if (SystemUtils.IS_OS_MAC) {
            extension = MAC_EXTENSION;
            dir = MAC;
        } else {
            throw logger.logAndReturnException(CoreMessages.LINUX_UPDATE, LogType.UPLOADER_ERROR);
        }
        initialize(PRODUCT_NAME + "/" + dir);
    }

    /**
     * @return true if there are updates
     * false if no updates are available
     */
    public Boolean checkForUpdates() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return versionCompare(PropertiesHelper.VERSION, version) < 0;
    }

    public String versionForUpdate() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return versionCompare(PropertiesHelper.VERSION, version) < 0 ? version : null;
    }

    /**
     * Compares two version strings.
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is numerically less than str2.
     * The result is a positive integer if str1 is numerically greater than str2.
     * The result is zero if the strings are numerically equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static Integer versionCompare(String str1, String str2) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            String[] value1 = str1.split("\\.");
            String[] value2 = str2.split("\\.");
            int i = 0;
            // set index to first non-equal ordinal or length of shortest version string
            while (i < value1.length && i < value2.length && value1[i].equals(value2[i])) {
                i++;
            }
            // compare first non-equal ordinal number
            if (i < value1.length && i < value2.length) {
                int diff = Integer.valueOf(value1[i]).compareTo(Integer.valueOf(value2[i]));
                return Integer.signum(diff);
            }
            // the strings are equal or one string is a substring of the other
            // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
            else {
                return Integer.signum(value1.length - value2.length);
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.VERSION_COMPARE_ERROR, e, LogType.UPLOADER_ERROR);
        }
    }

    public DownloadFile getDistribution() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        DownloadFile downloadFile = getDownloadFile(PRODUCT_NAME + "/" + dir + "/" + PRODUCT_NAME + "_" + version
                + "." + extension, extension);
        downloadFile.setDisplayFileName("distribution");
        return downloadFile;
    }

    public DownloadFile getUpdater() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String fileName = PRODUCT_NAME + "_updater_" + version + "." + UPDATER_EXTENSION;
        DownloadFile downloadFile = getDownloadFile(PRODUCT_NAME + "/" + fileName,
                Paths.get(FileUtils.getTempDirectoryPath(), fileName).toFile());
        downloadFile.setDisplayFileName("updater");
        return downloadFile;
    }
}
