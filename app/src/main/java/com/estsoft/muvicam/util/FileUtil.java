package com.estsoft.muvicam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by jaylim on 12/19/2016.
 */

public class FileUtil {

  public static void copyFile(File src, File dest) throws IOException {
    FileChannel inChannel = new FileInputStream(src).getChannel();
    FileChannel outChannel = new FileOutputStream(dest).getChannel();
    try {
      inChannel.transferTo(0, inChannel.size(), outChannel);
    } finally {
      if (inChannel != null)
        inChannel.close();
      if (outChannel != null)
        outChannel.close();
    }
  }
}
