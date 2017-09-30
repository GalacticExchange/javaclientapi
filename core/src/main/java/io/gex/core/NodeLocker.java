package io.gex.core;


import io.gex.core.exception.GexAuthException;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Paths;

//todo refactor
public class NodeLocker {

    private final static LogWrapper logger = LogWrapper.create(NodeLocker.class);

    public static void executeWithLock(GexExecutor executor) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        FileChannel channel;
        String fileName = "gex.lock";
        File file = Paths.get(PropertiesHelper.userHome, ".gex", fileName).toFile();
        try {
            file.createNewFile();
            channel = new RandomAccessFile(file, "rw").getChannel();
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.CREATE_LOCK_ERROR, e, LogType.LOCK_ERROR);
        }
        try {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                logger.logInfo(CoreMessages.LOCK + fileName, LogType.LOCK);
                try {
                    executor.execute();
                } catch (GexAuthException e) {
                    throw e;
                } catch (Exception e) {
                    throw logger.logAndReturnException(e, LogType.NODE_ACTION_ERROR);
                } finally {
                    lock.release();
                    logger.logInfo(CoreMessages.LOCK_RELEASE + fileName, LogType.LOCK);
                }
            } else {
                throw logger.logAndReturnException(CoreMessages.FILE_ALREADY_LOCKED, LogType.LOCK_ERROR);
            }
        } catch (OverlappingFileLockException e) {
            throw logger.logAndReturnException(CoreMessages.FILE_ALREADY_LOCKED, e, LogType.LOCK_ERROR);
        } catch (IOException e) {
            throw logger.logAndReturnException(e, LogType.LOCK_ERROR);
        }
    }

    public static String executeWithLockOutput(GexExecutorOutput executor) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        FileChannel channel;
        String fileName = "gex.lock";
        File file = Paths.get(PropertiesHelper.userHome, ".gex", fileName).toFile();
        try {
            file.createNewFile();
            channel = new RandomAccessFile(file, "rw").getChannel();
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.CREATE_LOCK_ERROR, e, LogType.LOCK_ERROR);
        }
        try {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                logger.logInfo(CoreMessages.LOCK + fileName, LogType.LOCK);
                try {
                    return executor.execute();
                } catch (Exception e) {
                    throw logger.logAndReturnException(e, LogType.NODE_ACTION_ERROR);
                } finally {
                    lock.release();
                    logger.logInfo(CoreMessages.LOCK_RELEASE + fileName, LogType.LOCK);
                }
            } else {
                throw logger.logAndReturnException(CoreMessages.FILE_ALREADY_LOCKED, LogType.LOCK_ERROR);
            }
        } catch (OverlappingFileLockException e) {
            throw logger.logAndReturnException(CoreMessages.FILE_ALREADY_LOCKED, e, LogType.LOCK_ERROR);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.LOCK_ERROR);
        }
    }
}
