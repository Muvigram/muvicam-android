package com.estsoft.muvicam.transcoder.wrappers;

import android.content.res.AssetFileDescriptor;

import java.io.FileDescriptor;

/**
 * Created by estsoft on 2017-01-19.
 */

public interface MediaTranscoder {

    void initVideoTarget(int interval, int frameRate, int bitrate, int rotation, int width, int height, boolean videoFlipping );
    void initAudioTarget( int sampleRate, int channelCount, int bitrate );

    void addSegment(String inputFilePath, long startTimeUs, long endTimeUs, int audioVolume  );
    void addLogoSegment(AssetFileDescriptor inputFile, long startTimeUs, long endTimeUs, int audioVolume);
    void addMusicSegment(String inputFilePath, long offset, int audioVolume );

    void startWork();



}
