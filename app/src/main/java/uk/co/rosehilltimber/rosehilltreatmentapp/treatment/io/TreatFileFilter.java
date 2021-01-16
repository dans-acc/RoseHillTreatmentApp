package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.io;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class TreatFileFilter implements FileFilter
{

    // The default file-must-exist state.
    private static final boolean DEFAULT_FILE_MUST_EXIST = true;

    // The regex used to identify the file extension.
    private static final String XLS_FILE_EXTENSION_REGEX = "^.+\\.xls$";
    private static final Pattern XLS_FILE_EXTENSION_PATTERN = Pattern.compile(XLS_FILE_EXTENSION_REGEX);

    // The regex used to check whether or not the filename contains a single week.
    private static final String SINGLE_WEEK_FILE_NAME_REGEX = "^([0-9]{4}_){2}[0-9]{1,2}$";
    private static final Pattern SINGLE_WEEK_FILE_NAME_PATTERN = Pattern.compile(SINGLE_WEEK_FILE_NAME_REGEX);

    // Similarly, the regex is used to check whether the file contains multiple weeks.
    private static final String MULTI_WEEK_FILE_NAME_REGEX = "^([0-9]{4}_){2}[0-9]{1,2}_[0-9]{1,2}$";
    private static final Pattern MULTI_WEEK_FILE_NAME_PATTERN = Pattern.compile(MULTI_WEEK_FILE_NAME_REGEX);

    private boolean mFileMustExist;

    public TreatFileFilter(final boolean mFileMustExist)
    {
        this.mFileMustExist = mFileMustExist;
    }

    public TreatFileFilter()
    {
        this(DEFAULT_FILE_MUST_EXIST);
    }

    public boolean fileMustExist()
    {
        return mFileMustExist;
    }

    public void setFileMustExist(final boolean mFileMustExist)
    {
        this.mFileMustExist = mFileMustExist;
    }

    @Override
    public boolean accept(@NonNull final File file)
    {
        // Check that the file is the appropriate format.
        if (mFileMustExist && !file.exists() && !file.canRead()) {
            return false;
        }else if (file.isDirectory()) {
            return false;
        }

        // Check whether the filename is empty of not.
        String filename = file.getName();
        if (filename.isEmpty()) {
            return false;
        }

        // Check whether or not the file has the appropriate file extension.
        if (!XLS_FILE_EXTENSION_PATTERN.matcher(filename).find()) {
            return false;
        }


        // Check whether the name matches the regex.
        filename = file.getName().substring(0, filename.lastIndexOf('.'));
        return SINGLE_WEEK_FILE_NAME_PATTERN.matcher(filename).matches()
                || MULTI_WEEK_FILE_NAME_PATTERN.matcher(filename).matches();
    }
}
