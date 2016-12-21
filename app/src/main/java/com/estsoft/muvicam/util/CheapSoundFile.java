package com.estsoft.muvicam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by jaylim on 12/21/2016.
 */


public abstract class CheapSoundFile {

  protected File mInputFile = null;

  public CheapSoundFile(File inputFile) throws IllegalStateException {
    mInputFile = inputFile;
  }

  public abstract void readFile() throws IOException;

  public abstract int getNumFrames();

  public abstract int getSamplesPerFrame();

  public abstract int[] getFrameGains();

  public abstract int getFileSizeBytes();

  public abstract int getAvgBitrateKbps();

  public abstract int getSampleRate();

  public abstract int getChannels();

  public abstract String getFiletype();

}
