package com.estsoft.muvigram.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import rx.Observable;
import timber.log.Timber;

/**
 * Sound files for mp3 format. It is compatible with MPEG 1/2 Audio Layer 3.
 *
 * Created by jaylim on 12/21/2016.
 */
public class MP3File extends SoundFile {

  // Member variables representing frame data
  private int mTotalFrameNum;
  private int[] mGlobalGains;
  // TODO - private int[] mLGlobalGains;
  // TODO - private int[] mRGlobalGains;
  private int mFileSize;
  private int mAvgBitRate;
  private int mGlobalSampleRate;
  private int mGlobalChannels;

  private int mMaxGain = -1;
  private int mMinGain = 257;

  // Member variables used during initialization
  private int mMaxFrameNum;
  private int mBitrateSum;

  public static Observable<File> create(File inputFile) {
    return Observable.create(subscriber -> {
      subscriber.onNext(inputFile);
      subscriber.onCompleted();
    });
  }


  public MP3File(File inputFile) throws IllegalStateException {
    super(inputFile);
    if (!isMP3(mInputFile.getName().toLowerCase())) {
      IllegalArgumentException iae = new IllegalArgumentException();
      Timber.e(iae, "File has illegal extension name.");
      throw iae;
    } else {
      try {
        readFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void moderateGains() {
    mMaxGain = -1;
    mMinGain = 257;

    int[] temp = new int[mTotalFrameNum];
    int[] histogram = new int[256];

    for (int i = 0; i < mTotalFrameNum; i++) {
      int val;
      if (i == 0) {
        val = (mGlobalGains[i] + mGlobalGains[i+1]) / 2;
      } else if (i == mTotalFrameNum - 1) {
        val = (mGlobalGains[i-1] + mGlobalGains[i]) / 2;
      } else if (i == mTotalFrameNum - 2 || i == 1){
        val = (mGlobalGains[i-1] + mGlobalGains[i] + mGlobalGains[i+1]) / 3;
      } else {
        val = (mGlobalGains[i-2] + mGlobalGains[i-1] + mGlobalGains[i] + mGlobalGains[i+1] + mGlobalGains[i+2]) / 5;
      }

      histogram[val]++;

      temp[i] = val;
    }
    mGlobalGains = temp;


    int median = 0;
    int mid = 256 / 2;

    int sum = 0;
    while (sum < mTotalFrameNum / 2) {
      sum += histogram[median++];
    }

    for (int i = 0; i < mTotalFrameNum; i++) {
      if (mGlobalGains[i] < median) {
        mGlobalGains[i] = mid * mGlobalGains[i] / median;
      } else {
        mGlobalGains[i] = mid + (256 - mid) * (mGlobalGains[i] - median) / (256 - median);
      }

    }

    int scaledMin;
    int scaledMax;

    scaledMin = 0;
    int lowerSum = 0;
    while (lowerSum < mTotalFrameNum * 5 / 100 && scaledMin < 100) {
      lowerSum += histogram[scaledMin++];
    }
    scaledMin--;

    scaledMax = 255;
    int upperSum = 0;
    while (upperSum < mTotalFrameNum * 5 / 100 && scaledMax > 155) {
      upperSum += histogram[scaledMax--];
    }
    scaledMax++;

    for (int i =0; i < mTotalFrameNum; i++) {
      if (mGlobalGains[i] < scaledMin) {
        mGlobalGains[i] = 0;
      }else if (mGlobalGains[i] > scaledMax) {
        mGlobalGains[i] = 255;
      } else {  /* min <= item <= max */
        mGlobalGains[i] = 255 * (mGlobalGains[i] - scaledMin) / (scaledMax - scaledMin);
      }

      mMaxGain = (mGlobalGains[i] > mMaxGain) ? mGlobalGains[i] : mMaxGain;
      mMinGain = (mGlobalGains[i] < mMinGain) ? mGlobalGains[i] : mMinGain;
    }
  }

  @Override
  public int getMaxGain() {
    return mMaxGain;
  }

  @Override
  public int getMinGain() {
    return mMinGain;
  }

  /**
   * The total number of frames extracted from music file and
   * processed by the internal algorithm..
   *
   * @return Total number of frames
   */
  public int getTotalFrameNum() {
    return mTotalFrameNum;
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
   * Sample rate of the file.
   *
   * @return The global sample rate.
   */
  @Override
  public int getSampleRate() {
    return mGlobalSampleRate;
  }

  public void readFile() throws IOException {
    mTotalFrameNum = 0;   // This is for counting the number of frames
    mMaxFrameNum = 64;  // This will grow as needed
    mGlobalGains = new int[mMaxFrameNum];

    mBitrateSum = 0;

    // Integer can represents up to 2GB, so that it is enough to use int type.
    mFileSize = (int) mInputFile.length();

    FileInputStream stream = new FileInputStream(mInputFile);

    int pos = 0;
    int offset = 0;
    byte[] buffer = new byte[BUFFER_LEN];
    while (pos + BUFFER_LEN < mFileSize) {
      int read = BUFFER_LEN;

      // Reads up to 16 bytes at a time and looks for a sync code (0xFF)
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
      int version;
      boolean crc;
      if (buffer[1] == xFA) {
        version = MPEG_V1_L3; // xFA, xFB means 11 of position (20, 19) - V1 L3
        crc = true;
      } else if (buffer[1] == xFB) {
        version = MPEG_V1_L3; // xFA, xFB means 11 of position (20, 19) - V1 L3
        crc = false;
      } else if (buffer[1] == xF2) {
        version = MPEG_V2_L3; // xF2, xF3 means 10 of position (20, 19) - V2 L3
        crc = true;
      } else if (buffer[1] == xF3) {
        version = MPEG_V2_L3; // xF2, xF3 means 10 of position (20, 19) - V2 L3
        crc = false;
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
      // One slot is 4-byte-long for Layer I, and one-byte-long for Layer II and Layer III.
      // When you are reading MPEG file you must calculate this to be able to find each
      // consecutive frame. Remember, frame length may change from frame to frame
      // due to padding or bitrate switching.
      int frameSizeInByte = 144 * (bitRate * 1000) / sampleRate + padding;

      // (8) ignore private bit

      // check the number of channels
      if ((buffer[3] & 0xC0) == 0xC0) { /* 11 - Single channel */
        mGlobalChannels = CHANNEL_SINGLE;
      } else {
        mGlobalChannels = CHANNEL_DUAL;
      }

      // skip remain bits in header, and CRC bits, so that go to side information bits.
      int headerSize = 4;
      if (crc) {
        headerSize += 2;
      }
      read += skipBytes(buffer, headerSize, stream);

      // get global gain
      int globalGain = 0;
      if (version == MPEG_V1_L3) {
        if (mGlobalChannels == CHANNEL_SINGLE) {
          // Bits(136) [9 5 4] [12 9 '8' 30] [12 9 '8' 30]
          // |8|8|8|7 1| |7 1|8|8|8| |8|8|8|2 6| |2 6|8|8|8|
          globalGain += ((buffer[3]  & 0x01) << 7) + ((buffer[4]  & (0xFF-0x01)) >> 1);
          globalGain += ((buffer[11] & 0x3F) << 2) + ((buffer[12] & (0xFF-0x3F)) >> 6);
          // globalGain += readBits(buffer[3], buffer[4], 1);
          // globalGain += readBits(buffer[11], buffer[12], 6);

          read += stream.skip(1L);
          globalGain /= 2;
        } else /* mGlobalChannels == CHANNEL_SINGLE */ {
          // Bits(256) [9 3 8] [12 9 '8' 30] [12 9 '8' 30] [12 9 '8' 30] [12 9 '8' 30]
          // |8|8|8| 8 | | 8 |1 7|1 7|8| |8|8|8| 8 | |4 4|4 4|8|8|
          // |8|8|8|7 1| |7 1| 8 | 8 |8| |8|8|8|2 6| |2 6| 8 |8|8|
          globalGain += ((buffer[5]  & 0x7F) << 1) + ((buffer[6]  & (0xFF-0x7F)) >> 7);
          globalGain += ((buffer[12] & 0x0F) << 4) + ((buffer[13] & (0xFF-0x0F)) >> 4);
          // globalGain += readBits(buffer[5], buffer[6], 7);
          // globalGain += readBits(buffer[12], buffer[13], 4);
          read += skipBytes(buffer, BUFFER_LEN, stream);
          globalGain += ((buffer[3]  & 0x01) << 7) + ((buffer[4]  & (0xFF-0x01)) >> 1);
          globalGain += ((buffer[11] & 0x3F) << 2) + ((buffer[12] & (0xFF-0x3F)) >> 6);
          // globalGain += readBits(buffer[3], buffer[4], 1);
          // globalGain += readBits(buffer[11], buffer[12], 6);

          globalGain /= 4;
        }
      } else { /* version == MPEG_V2_L3 */
        if (mGlobalChannels == CHANNEL_SINGLE) {
          // Bits(72) [8 1] [12 9 '8' 34]
          // |8|8|8|6 2| |6 2|8|8|8|
          // |8|
          globalGain += ((buffer[3]  & 0x03) << 6) + ((buffer[4]  & (0xFF-0x03)) >> 2);
          // globalGain += readBits(buffer[3],buffer[4], 2);

          read += stream.skip(1L);

        } else /* mGlobalChannels == CHANNEL_SINGLE */ {
          // Bits(136) [8 2] [12 9 '8' 34] [12 9 '8' 34]
          // |8|8|8|7 1| |7 1|8|8|8| |8|8|8|6 2| |6 2|8|8|8|
          // |8|
          globalGain += ((buffer[3]   & 0x01) << 7) + ((buffer[4]   & (0xFF-0x01)) >> 1);
          globalGain += ((buffer[11]  & 0x03) << 6) + ((buffer[12]  & (0xFF-0x03)) >> 2);
          // globalGain += readBits(buffer[3],buffer[4], 1);
          // globalGain += readBits(buffer[11],buffer[12], 2);

          read += stream.skip(1L);
          globalGain /= 2;

        }
      }

      mMaxGain = (globalGain > mMaxGain) ? globalGain : mMaxGain;
      mMinGain = (globalGain < mMinGain) ? globalGain : mMinGain;

      mBitrateSum += bitRate;

      if (globalGain < 0 || globalGain > 255) {
        RuntimeException re = new RuntimeException();
        Timber.e(re, "m/readFile Global gain, in frame #%d, is out of bound : %d", mTotalFrameNum, globalGain);
        throw re;
      }
      mGlobalGains[mTotalFrameNum] = globalGain;

      mTotalFrameNum++;
      if (mTotalFrameNum == mMaxFrameNum) {

        mAvgBitRate = mBitrateSum / mTotalFrameNum;
        int expectedTotalFrameSize = ((mFileSize / mAvgBitRate) * sampleRate) / 144000;
        int newMaxFrames = expectedTotalFrameSize * 11 / 10;

        if (newMaxFrames < mMaxFrameNum * 2)
          newMaxFrames = mMaxFrameNum * 2;

        int[] newGains = new int[newMaxFrames];
        for (int i = 0; i < mTotalFrameNum; i++) {
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
    if (mTotalFrameNum > 0)
      mAvgBitRate = mBitrateSum / mTotalFrameNum;
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

  private final static int CHANNEL_SINGLE = 1;
  private final static int CHANNEL_DUAL = 2;

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
      IllegalArgumentException iae = new IllegalArgumentException();
      Timber.e(iae, "m/isMP3 Cannot resolve the file name.");
      throw iae;
    }
    return "mp3".equals(tokens[tokens.length - 1]);
  }

  public static void normalize(int[] items, float margin) {
    // TODO
  }

  public static int skipBytes(byte[] buffer, int n, FileInputStream inputStream) throws IOException {
    for (int i = 0; i < buffer.length- n; i++) {
      buffer[i] = buffer[i + n];
    }
    int offset = buffer.length - n;

    int read = 0;
    while (offset < buffer.length) {
      int cnt = inputStream.read(buffer, offset, buffer.length - offset);
      offset += cnt;
      read += cnt;
    }

    return read;
  }

}
