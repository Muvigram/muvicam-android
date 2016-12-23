package com.estsoft.muvicam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * ADT for sound files.
 *
 * Created by jaylim on 12/21/2016.
 */


public abstract class SoundFile {

  protected File mInputFile = null;

  public SoundFile(File inputFile) throws IllegalStateException {
    mInputFile = inputFile;
  }

  public abstract int getMaxGain();

  public abstract int getMinGain();

  public abstract void readFile() throws IOException;

  public abstract int getTotalFrameNum();

  public abstract int getSamplesPerFrame();

  public abstract int[] getGlobalGains();

  public abstract int getFileSizeBytes();

  public abstract int getAvgBitrate();

  public abstract int getSampleRate();

  public abstract int getChannels();

  public abstract String getFiletype();

}
