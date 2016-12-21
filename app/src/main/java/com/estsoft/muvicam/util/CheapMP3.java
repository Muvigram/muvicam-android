package com.estsoft.muvicam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by jaylim on 12/21/2016.
 */

public class CheapMP3 extends CheapSoundFile {

  // Member variables representing frame data
  private int mNumFrames;
  private int[] mFrameGains;
  private int mFileSize;
  private int mAvgBitRate;
  private int mGlobalSampleRate;
  private int mGlobalChannels;

  // Member variables used during initialization
  private int mMaxFrames;
  private int mBitrateSum;
  private int mMinGain;
  private int mMaxGain;

  public CheapMP3(File inputFile) throws IllegalStateException {
    super(inputFile);
    if (!isMP3(mInputFile.getName().toLowerCase())) {
      IllegalArgumentException e = new IllegalArgumentException();
      Timber.e(e, "File has illegal extension name.");
      throw e;
    } else {
      try {
        readFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public int getNumFrames() {
    return mNumFrames;
  }

  @Override
  public int getSamplesPerFrame() {
    return 1152;
  }

  @Override
  public int[] getFrameGains() {
    return mFrameGains;
  }

  @Override
  public int getFileSizeBytes() {
    return mFileSize;
  }

  @Override
  public int getAvgBitrateKbps() {
    return mAvgBitRate;
  }

  @Override
  public int getSampleRate() {
    return mGlobalSampleRate;
  }

  @Override
  public int getChannels() {
    return mGlobalChannels;
  }

  @Override
  public String getFiletype() {
    return "mp3";
  }

  public void readFile() throws IOException {
    mNumFrames = 0;   // This is for counting the number of frames
    mMaxFrames = 64;  // This will grow as needed
    mFrameGains = new int[mMaxFrames];

    mMinGain = 255;
    mMaxGain = 0;

    mBitrateSum = 0;

    // No need to handle filesizes larger than can fit in a 32-bit int
    mFileSize = (int)mInputFile.length();

    FileInputStream stream = new FileInputStream(mInputFile);

    int pos = 0;
    int offset = 0;
    byte[] buffer = new byte[BUFFER_LEN];
    while (pos < mFileSize - BUFFER_LEN) {

      // Read 12 bytes at a time and look for a sync code (0xFF)
      // Ex. FF FB E2 44 F1 06 07 F9 7E D3 93 6F
      while (offset < BUFFER_LEN) {
        offset += stream.read(buffer, offset, BUFFER_LEN - offset);
      }

//      StringBuilder stringBuilder = new StringBuilder("");
//      for (byte b : buffer) {
//        stringBuilder.append(" " + b + "[" + Integer.toHexString(b) + "]");
//      }
//      Log.e("DECODING", stringBuilder.toString());

      // [First byte] - look for a sync code (0xFF) and
      // pose bufferOffset where the sync code is (0xFF)
      //
      // if there is no sync code at position 0. shift the byte data left
      // to put the first-appeared 0xFF code, if any, into leftmost position.
      // And read more byte from stream source.
      int bufferOffset = 0;
      while (bufferOffset < BUFFER_LEN && buffer[bufferOffset] != xFF)
        bufferOffset++;
      if (bufferOffset > 0) {
        for (int i = 0; i < BUFFER_LEN - bufferOffset; i++)
          buffer[i] = buffer[bufferOffset + i];
        pos += bufferOffset;
        offset = BUFFER_LEN - bufferOffset;
        continue;
      }

      // [Second byte] - Check for V1L3 or V2L3
      int mpgVersion = 0;
      if (buffer[1] == xFA || buffer[1] == xFB) {
        mpgVersion = MPEG_V1_L3; // xFA, xFB means 11 of position (20, 19) - V1 L3
      } else if (buffer[1] == xF2|| buffer[1] == xF3) {
        mpgVersion = MPEG_V2_L3; // xF2, xF3 means 10 of position (20, 19) - V2 L3
      } else { // Invalid MPEG version.
        bufferOffset = 1;
        for (int i = 0; i < BUFFER_LEN - bufferOffset; i++)
          buffer[i] = buffer[bufferOffset + i];
        pos += bufferOffset;
        offset = BUFFER_LEN - bufferOffset;
        continue;
      }

      // [Third byte] has the bitrate and samplerate
      int bitRate;
      int sampleRate;
      if (mpgVersion == MPEG_V1_L3) {
        bitRate = BITRATES_V1_L3[(buffer[2] & 0xF0) >> 4];    /* Get (15-12) position bits */
        sampleRate = SAMPLERATES_V1[(buffer[2] & 0x0C) >> 2]; /* Get (11-10) position bits */
      } else {
        bitRate = BITRATES_V2_L3[(buffer[2] & 0xF0) >> 4];    /* Get (15-12) position bits */
        sampleRate = SAMPLERATES_V2[(buffer[2] & 0x0C) >> 2]; /* Get (11-10) position bits */
      }

      if (bitRate == 0 || sampleRate == 0) {
        bufferOffset = 2;
        for (int i = 0; i < 12 - bufferOffset; i++)
          buffer[i] = buffer[bufferOffset + i];
        pos += bufferOffset;
        offset = 12 - bufferOffset;
        continue;
      }

      // Padding bit (9), Private bit (8), and Channel mode (7, 6)
      // Assume frame is valid from here.
      mGlobalSampleRate = sampleRate;

      // padding bit (9) : 0 - frame is not padded, 1 - frame is padded
      // Padding is used to fit the bit rates exactly.
      // For Layer I slot is 32 bits long, for Layer II and Layer III slot is 8 bits long.
      int padding = (buffer[2] & 2) >> 1;

      int frameLen = 144 * bitRate * 1000 / sampleRate + padding;

      // Channel mode (7, 6)
      int gain;
      if ((buffer[3] & 0xC0) == 0xC0) { /* 11 - Single channel */
        mGlobalChannels = 1;
        if (mpgVersion == MPEG_V1_L3) {
          gain = ((buffer[10] & 0x01) << 7) +
              ((buffer[11] & 0xFE) >> 1);
        } else {
          gain = ((buffer[9] & 0x03) << 6) +
              ((buffer[10] & 0xFC) >> 2);
        }
      } else { /* else - dual channel, joint stereo, stereo */
        mGlobalChannels = 2;
        if (mpgVersion == MPEG_V1_L3) {
          gain = ((buffer[9]  & 0x7F) << 1) +
              ((buffer[10] & 0x80) >> 7);
        } else {
          gain = 0;  // ???
        }
      }

      mBitrateSum += bitRate;

      mFrameGains[mNumFrames] = gain;
      if (gain < mMinGain)
        mMinGain = gain;
      if (gain > mMaxGain)
        mMaxGain = gain;

      mNumFrames++;
      if (mNumFrames == mMaxFrames) {

        mAvgBitRate = mBitrateSum / mNumFrames;
        int totalFramesGuess =
            ((mFileSize / mAvgBitRate) * sampleRate) / 144000;
        int newMaxFrames = totalFramesGuess * 11 / 10;
        if (newMaxFrames < mMaxFrames * 2)
          newMaxFrames = mMaxFrames * 2;

        int[] newOffsets = new int[newMaxFrames];
        int[] newLens = new int[newMaxFrames];
        int[] newGains = new int[newMaxFrames];
        for (int i = 0; i < mNumFrames; i++) {
          newGains[i] = mFrameGains[i];
        }
        mFrameGains = newGains;
        mMaxFrames = newMaxFrames;
      }

      stream.skip(frameLen - 12);

      pos += frameLen;
      offset = 0;
    }

    // We're done reading the file, do some postprocessing
    if (mNumFrames > 0)
      mAvgBitRate = mBitrateSum / mNumFrames;
    else
      mAvgBitRate = 0;
  }

  private final static int BUFFER_LEN = 12;  /* Buffer length */

  // (31-24) : Frame sync code (all bits set)
  private final static int xFF = 0xFFFFFFff; /* 0xFF; [11111111; -1  */

  // (23-21) : Frame sync code (continued from 31-24)
  // (20, 19) : MPEG Audio version ID, (18, 17) Layer description, (16) protection bit
  private final static int xFA = 0xFFFFFFfa; /* 0xFa; 111][11][01][0]; -5  */
  private final static int xFB = 0xFFFFFFfb; /* 0xFb; 111][11][01][1]; -6  */
  private final static int xF2 = 0xFFFFFFf2; /* 0xF2; 111][10][01][0]; -14 */
  private final static int xF3 = 0xFFFFFFf3; /* 0xF3; 111][10][01][1]; -13 */

  private final static int MPEG_V1_L3 = 1;
  private final static int MPEG_V2_L3 = 2;

  static private int BITRATES_V1_L3[] = {
      0,  32,  40,  48,  56,  64,  80,  96,
      112, 128, 160, 192, 224, 256, 320,  0 };
  static private int BITRATES_V2_L3[] = {
      0,   8,  16,  24,  32,  40,  48,  56,
      64,  80,  96, 112, 128, 144, 160, 0 };
  static private int SAMPLERATES_V1[] = {
      44100, 48000, 32000, 0 };
  static private int SAMPLERATES_V2[] = {
      22050, 24000, 16000, 0 };

  public static boolean isMP3(String lowercaseName) throws IllegalStateException {
    String[] tokens = lowercaseName.toLowerCase().split("\\.");
    if (tokens.length < 2) {
      IllegalArgumentException e = new IllegalArgumentException();
      Timber.e(e, "Cannot resolve the file name.");
      throw e;
    }
    return "mp3".equals(tokens[tokens.length - 1]);
  }

}
