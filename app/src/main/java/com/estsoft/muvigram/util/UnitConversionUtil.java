package com.estsoft.muvigram.util;

/**
 * Created by jaylim on 27/12/2016.
 */

public class UnitConversionUtil {

    public static int secToMillisec(float sec) {
        return (int) (sec * 1000);
    }

    public static float millisecToSec(int millisec) {
        return millisec / 1000.0f;
    }

}
