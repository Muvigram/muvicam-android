package com.estsoft.muvicam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * Sound files for mp3 format. It is compatible with MPEG 1/2 Audio Layer 3.
 *
 * Created by jaylim on 12/21/2016.
 */
public class MP3File extends SoundFile {

  // Member variables representing frame data
  private int mCurFrameNum;
  private int[] mGlobalGains;
  private int mFileSize;
  private int mAvgBitRate;
  private int mGlobalSampleRate;
  private int mGlobalChannels;

  // Member variables used during initialization
  private int mMaxFrameNum;
  private int mBitrateSum;

  public MP3File(File inputFile) throws IllegalStateException {
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

  /**
   * The total number of frames extracted from music file and
   * processed by the internal algorithm..
   *
   * @return Total number of frames
   */
  public int getCurFrameNum() {
    return mCurFrameNum;
  }

  /**
   * Frame size is the number of samples contained in a frame. It is constant
   * and always 384 samples for Layer I and 1152 samples for Layer II and Layer III.
   *
   * @return Constant {@code 1152}
   */
  @Override
  public int getSamplesPerFrame() {
    return 1152;
  }

  /**
   * Global gains which is decoded from each frame's side information. This value
   * specifies the quantization step size of each frame.
   * </p>
   * The global gains will be used to represent the loudness of sound in each frame.
   *
   * @return The global gains corresponding to each frame.
   */
  public int[] getGlobalGains() {
    return mGlobalGains;
  }

  /**
   * Size of input file.
   *
   * @return Size of input file.
   */
  @Override
  public int getFileSizeBytes() {
    return mFileSize;
  }

  /**
   * Average bitrate of the file.
   *
   * @return Average bit rate.
   */
  @Override
  public int getAvgBitrate() {
    return mAvgBitRate;
  }

  /**
   * Sample rate of the file.
   *
   * @return The global sample rate.
   */
  @Override
  public int getSampleRate() {
    return mGlobalSampleRate;
  }

  /**
   * The number of channels.
   *
   * @return The number of channels.
   */
  @Override
  public int getChannels() {
    return mGlobalChannels;
  }

  @Override
  public String getFiletype() {
    return "mp3";
  }

  public void readFile() throws IOException {
    mCurFrameNum = 0;   // This is for counting the number of frames
    mMaxFrameNum = 64;  // This will grow as needed
    mGlobalGains = new int[mMaxFrameNum];

    mBitrateSum = 0;

    // No need to handle filesizes larger than can fit in a 32-bit int
    mFileSize = (int)mInputFile.length();

    FileInputStream stream = new FileInputStream(mInputFile);

    int pos = 0;
    int offset = 0;
    byte[] buffer = new byte[BUFFER_LEN];
    while (pos < mFileSize - BUFFER_LEN) {
      int read = BUFFER_LEN;
      // Read 12 bytes at a time and look for a sync code (0xFF)
      // Ex. FF FB E2 44 F1 06 07 F9 7E D3 93 6F
      while (offset < BUFFER_LEN) {
        offset += stream.read(buffer, offset, BUFFER_LEN - offset);
      }

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
      int version = 0;
      if (buffer[1] == xFA || buffer[1] == xFB) {
        version = MPEG_V1_L3; // xFA, xFB means 11 of position (20, 19) - V1 L3
      } else if (buffer[1] == xF2|| buffer[1] == xF3) {
        version = MPEG_V2_L3; // xF2, xF3 means 10 of position (20, 19) - V2 L3
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
      if (version == MPEG_V1_L3) {
        bitRate = BITRATES_V1_L3[(buffer[2] & 0xF0) >> 4];    /* Get (15-12) position bits */
        sampleRate = SAMPLERATES_V1[(buffer[2] & 0x0C) >> 2]; /* Get (11-10) position bits */
      } else {
        bitRate = BITRATES_V2_L3[(buffer[2] & 0xF0) >> 4];    /* Get (15-12) position bits */
        sampleRate = SAMPLERATES_V2[(buffer[2] & 0x0C) >> 2]; /* Get (11-10) position bits */
      }

      if (bitRate == 0 || sampleRate == 0) {
        bufferOffset = 2;
        for (int i = 0; i < BUFFER_LEN - bufferOffset; i++)
          buffer[i] = buffer[bufferOffset + i];
        pos += bufferOffset;
        offset = BUFFER_LEN - bufferOffset;
        continue;
      }

      // Assume frame is valid after this line.

      mGlobalSampleRate = sampleRate;

      // padding bit (9) : 0 - frame is not padded, 1 - frame is padded
      // Padding is used to fit the bit rates exactly.
      // For Layer I slot is 32 bits long, for Layer II and Layer III slot is 8 bits long.
      int padding = (buffer[2] & 2) >> 1;

      // Frame length is length of a frame when compressed. It is calculated in slots.
      // One slot is 4 bytes long for Layer I, and one byte long for Layer II and Layer III.
      // When you are reading MPEG file you must calculate this to be able to find each
      // consecutive frame. Remember, frame length may change from frame to frame
      // due to padding or bitrate switching.
      int frameSizeInByte = 144 * (bitRate * 1000) / sampleRate + padding;

      // (8) ignore private bit

      // (7, 6) : Channel mode, ignore (5, 4) which is only used in joint stereo mode.
      int globalGain;
      if ((buffer[3] & 0xC0) == 0xC0) { /* 11 - Single channel */
        mGlobalChannels = 1;
        if (version == MPEG_V1_L3) { /* 2 channels for 2 granule */
          // First global gain from side information : [10:11] - 0000|0001|1111|1110
          globalGain = 0;
          for (int i = 0; i < 2 * mGlobalChannels; i++) {
            globalGain += ((buffer[10] & 0x01) << 7) + ((buffer[11] & 0xFE) >> 1);
            read += stream.skip(43L);
            read += stream.read(buffer, 0, BUFFER_LEN);
          }
          globalGain /= (2 * mGlobalChannels);

        } else { /* MPEG_V2_L3 */
          // First global gain from side information : [09:10] - 0000|0011|1111|1100
          globalGain = ((buffer[9]  & 0x03) << 6) + ((buffer[10] & 0xFC) >> 2);
        }

      } else { /* else - dual channel, joint stereo, stereo */
        mGlobalChannels = 2;
        if (version == MPEG_V1_L3) {
          // First global gain from side information : [11:12] - 0111|1111|1000|0000
          globalGain = 0;
          for (int i = 0; i < 2 * mGlobalChannels; i++) {
            globalGain += ((buffer[11] & 0x7F) << 1) + ((buffer[12] & 0x80) >> 7);
            read += stream.skip(47L);
            read += stream.read(buffer, 0, BUFFER_LEN);
          }
          globalGain /= (2 * mGlobalChannels);

        } else { /* MPEG_V2_L3 */
          // First global gain from side information : [09:10] - 0000|0001|1111|1110
          globalGain = 0;
          for (int i = 0; i < mGlobalChannels; i++) {
            globalGain += ((buffer[9]  & 0x01) << 1) + ((buffer[10] & 0xFE) >> 7);
            read += stream.skip(47L);
            read += stream.read(buffer, 0, BUFFER_LEN);
          }
          globalGain /= mGlobalChannels;

        } // if (version == MPEG_VI_L3)
      } // if ((buffer[3] & 0xC0) == 0xC0)

      mBitrateSum += bitRate;

      if (globalGain < 0 || globalGain > 255) {
        RuntimeException e = new RuntimeException();
        Timber.e(e, "Global gain, in frame #%d, is out of bound : %d", mCurFrameNum, globalGain);
        throw e;
      }
      mGlobalGains[mCurFrameNum] = globalGain;

      mCurFrameNum++;
      if (mCurFrameNum == mMaxFrameNum) {

        mAvgBitRate = mBitrateSum / mCurFrameNum;
        int expectedTotalFrameSize = ((mFileSize / mAvgBitRate) * sampleRate) / 144000;
        int newMaxFrames = expectedTotalFrameSize * 11 / 10;

        if (newMaxFrames < mMaxFrameNum * 2)
          newMaxFrames = mMaxFrameNum * 2;

        int[] newGains = new int[newMaxFrames];
        for (int i = 0; i < mCurFrameNum; i++) {
          newGains[i] = mGlobalGains[i];
        }
        mGlobalGains = newGains;
        mMaxFrameNum = newMaxFrames;
      }

      stream.skip(frameSizeInByte - read);

      pos += frameSizeInByte;
      offset = 0;
    }

    // We're done reading the file, do some postprocessing
    if (mCurFrameNum > 0)
      mAvgBitRate = mBitrateSum / mCurFrameNum;
    else
      mAvgBitRate = 0;
  }

  private final static int BUFFER_LEN = 16;  /* Buffer length */

  // (31-24) : Frame sync code (all bits set)
  private final static int xFF = 0xFFFFFFff; /* 0xFF; [11111111; -1  */

  // (23-21) : Frame sync code (continued from 31-24) 111]
  // (20, 19) : MPEG Audio version ID, (18, 17) : Layer description, (16) : protection bit
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
