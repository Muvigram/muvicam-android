package com.estsoft.muvicam.transcoder.utils;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;


import com.estsoft.muvicam.transcoder.wrappers.ProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by estsoft on 2016-12-06.
 */

public class TranscodeUtils {
    private static final String TAG = "TranscodeUtils";
    private static final String CASHING_FILE_NAME = "transcoded.mp4";
    private static final String VIDEO = "video/";
    private static final String AUIDO = "audio/";
    public static final int MODE_VIDEO = -10;
    public static final int MODE_AUDIO = -20;


    public static MediaFormat getFirstTrack(MediaExtractor extractor, int mode) {
        int trackCount = extractor.getTrackCount();
        String formatMode = mode == MODE_VIDEO ? VIDEO : AUIDO;
        for (int i = 0; i < trackCount; i ++) {
            MediaFormat format = extractor.getTrackFormat( i );
            if (format.getString( MediaFormat.KEY_MIME ).startsWith(formatMode)) {
                return format;
            }
        }
        return null;
    }

    public static int getFirstTrackIndex(MediaExtractor extractor, int mode) {
        int trackCount = extractor.getTrackCount();
        String formatMode = mode == MODE_VIDEO ? VIDEO : AUIDO;
        for (int i = 0; i < trackCount; i ++) {
            MediaFormat format = extractor.getTrackFormat( i );
            if (format.getString( MediaFormat.KEY_MIME ).startsWith(formatMode)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean checkSampleRate( List<String> filePaths ) {
        MediaExtractor extractor = new MediaExtractor();
        MediaFormat audioFormat = null;
        int standardSampleRate = -1;
        for ( String filePath : filePaths ) {
            try {
                extractor.setDataSource(filePath);
            } catch ( IOException e ){
                return false;
            }
            audioFormat = getFirstTrack(extractor, MODE_AUDIO);
            if ( audioFormat == null ) return false;
            if (standardSampleRate == -1) standardSampleRate = audioFormat.getInteger( MediaFormat.KEY_SAMPLE_RATE );
            else {
                if (standardSampleRate != audioFormat.getInteger( MediaFormat.KEY_SAMPLE_RATE )) return false;
            }
        }
        return true;
    }

    public static boolean deleteCashingFile( Context context ) {
        return deleteCashingFile( context, CASHING_FILE_NAME );
    }
    public static boolean deleteCashingFile(Context context, String fileName ) {
        File file = new File(context.getExternalFilesDir(null), fileName);
        if (file.exists()) return file.delete();
        return false;
    }

    public static String getAppCashingFile(Context context ) {
        return getAppCashingFile( context, CASHING_FILE_NAME);
    }

    public static String getAppCashingFile(Context context, String fileName ) {
        File cashingDir = context.getExternalFilesDir(null);
        File cashingFile = new File( cashingDir, fileName );
        Log.d(TAG, "getAppCashingFile: " + cashingFile.getAbsolutePath());
        return cashingFile.getAbsolutePath();
    }

    public static String distinctCodeByCurrentTime(String prefix, String surfix ) {
        String name = "";
        if (!prefix.equals("")) name = prefix + "_";
        Calendar calendar = Calendar.getInstance();
        name += calendar.get( Calendar.YEAR ) ;
        name += String.format(Locale.US, "%02d", calendar.get( Calendar.MONTH ) + 1) ;
        name += String.format(Locale.US, "%02d", calendar.get( Calendar.DAY_OF_MONTH )) + "_";
        name += String.format(Locale.US, "%02d", calendar.get( Calendar.HOUR_OF_DAY))
                + String.format(Locale.US, "%02d", calendar.get( Calendar.MINUTE ))
                + String.format(Locale.US, "%02d", calendar.get( Calendar.SECOND ));
        Log.d(TAG, "distinctCodeByCurrentTime: " + name);
        return name + surfix;
    }

    public static void storeInGallery(String sourcePath, String targetName, Context context, ProgressListener listener ) {
        File DCIMdir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM );
        File targetFile = new File(DCIMdir, "Camera/" + targetName );
        InputStream is;
        OutputStream os;
        int fileWeight = 0;
        int fileProgressed = 0;
        int copyUnit = 1024;
        try {
            is = new FileInputStream( sourcePath );
            os = new FileOutputStream( targetFile );
            fileWeight = is.available() / copyUnit;
            byte[] data = new byte[ copyUnit ];
            listener.onStart(-1);
            while ( is.read( data ) > 0 ) {
                os.write( data );
                fileProgressed ++;
                if (fileProgressed % copyUnit == 0) {
                    if (listener != null) listener.onProgress( -1, fileProgressed * 100 / fileWeight );
                }
            }
            is.close();
            os.close();
        } catch ( IOException e ) {
            listener.onError( e );
            e.printStackTrace();

        }
        MediaScannerConnection.scanFile(context, new String[] { targetFile.getAbsolutePath() }, null, null);
        if (listener != null) listener.onComplete(-1);

    }

    public static void printInformationOf( MediaFormat format ) {
        Log.d(TAG, "printInformationOf: " + format.toString());
        Log.d(TAG, "printInformationOf: ----------------------------------------------------------- ");
//        Log.d(TAG, "printInformationOf: " + format.getString( MediaFormat.KEY_MIME));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_MAX_INPUT_SIZE));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_BIT_RATE));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_CHANNEL_COUNT));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_SAMPLE_RATE));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_PCM_ENCODING));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_IS_ADTS));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_PROFILE));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_SBR_MODE));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_DRC_TARGET_REFERENCE_LEVEL));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_ENCODED_TARGET_LEVEL));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_DRC_BOOST_FACTOR));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_DRC_ATTENUATION_FACTOR));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_DRC_HEAVY_COMPRESSION));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_AAC_MAX_OUTPUT_CHANNEL_COUNT));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_CHANNEL_MASK));
//        Log.d(TAG, "printInformationOf: " + format.getInteger( MediaFormat.KEY_FLAC_COMPRESSION_LEVEL));
    }

//    public interface ProgressListener {
//        void onProgress(int percentage);
//        void onComplete();
//        void onError(Exception e);
//    }

}
