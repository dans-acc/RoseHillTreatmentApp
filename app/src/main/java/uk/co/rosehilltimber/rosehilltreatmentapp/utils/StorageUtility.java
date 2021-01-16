package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.Nullable;
import android.util.Log;


public class StorageUtility
{

    private static final String ENVIRONMENT_DOCUMENT_DIRECTORY = Environment.DIRECTORY_DOCUMENTS;

    private static final String XLS_DOCUMENT_DIRECTORY = "xls";
    private static final String DATABASE_DOCUMENT_DIRECTORY = "database";

    private static volatile StorageUtility sInstance;

    private final Context mApplicationContext;

    private File mExternalXLSDirectory;
    private File mExternalDatabaseDirectory;

    private StorageUtility(@NonNull final Context mApplicationContext)
            throws RuntimeException
    {
        // Ensures that there is only one instance of this utility.
        if (sInstance != null) {
            throw new RuntimeException("StorageUtility can only be instantiated once.");
        }

        this.mApplicationContext = mApplicationContext;
    }

    @NonNull
    public static StorageUtility getInstance(@NonNull final Context context)
    {
        // Attempt to create an instance for the storage related utilities.
        if (sInstance == null) {
            synchronized (StorageUtility.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                try {
                    sInstance = new StorageUtility(context.getApplicationContext());
                } catch (final RuntimeException e) {
                    e.printStackTrace();
                    return sInstance;
                }
            }
        }

        // TODO: check that the media is mounted!

        return sInstance;
    }

    @NonNull
    public Context getApplicationContext()
    {
        return mApplicationContext;
    }

    public File getExternalXLSDirectory()
    {
        final String sdCardStatus = Environment.getExternalStorageState();
        if (!sdCardStatus.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        try {
            mExternalXLSDirectory = initStorageDir(new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    XLS_DOCUMENT_DIRECTORY
            ));
            return mExternalXLSDirectory;
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getExternalDatabaseDirectory()
    {
        return mExternalDatabaseDirectory;
    }

    @NonNull
    private File initStorageDir(@NonNull final File file)
            throws IOException
    {
        // Make the appropriate checks for the directory in question.
        if (file.exists() && !file.isDirectory()) {
            throw new IOException("Unable to init storage dir at filepath: " + file.getAbsolutePath());
        } else if (file.exists() && !file.canRead()) {
            throw new IOException("Unable to read storage dir at path: " + file.getAbsolutePath());
        } else if (file.exists()) {
            return file;
        }

        // Make the directory.
        if (!file.mkdirs()) {
            throw new IOException("Failed to create storage directory at filepath: " + file.getAbsolutePath());
        }

        return file;
    }

    public static boolean isDirChild(@NonNull final File dir, @NonNull final String filename, final boolean shouldExist)
    {
        return StorageUtility.isDirChild(dir, new File(dir, filename), shouldExist);
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isDirChild(@NonNull final File dir, @NonNull final File file, final boolean shouldExist)
    {
        // Check whether the file is a child within the directory.
        boolean child = false;
        try {
            child = file.getCanonicalPath().startsWith(dir.getCanonicalPath() + File.separator);
            if (!shouldExist) {
                return child;
            }
        } catch (final IOException e) {
            return false;
        }
        return child && file.exists();
    }

    @NonNull
    public static List<File> listDirFiles(@NonNull final File dir, @NonNull final FileFilter fileFilter)
            throws FileNotFoundException, SecurityException, IOException
    {
        // Check that we can read the directory
        if (!dir.exists()) {
            throw new FileNotFoundException(String.format(
                    "Unable to read list files for dir: %s. Dir: exists: %b; not directory: %b.",
                    dir.getPath(), dir.exists(), dir.isDirectory()
            ));
        }

        // By definition, listFiles will throw a SecurityException if files cannot be read.
        final File[] readFiles = dir.listFiles(fileFilter);
        if (readFiles == null) {
            throw new IOException(String.format(
                    "Failed to read file: %s. File type must be a directory.",
                    dir.getPath()
            ));
        }

        // Convert the array to a list of out convenience.
        final List<File> readFilesArrayList = new ArrayList<>(readFiles.length);
        Collections.addAll(readFilesArrayList, readFiles);
        return readFilesArrayList;
    }
}
