package com.estsoft.muvicam.transcoder.wrappers;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.estsoft.muvigram.transcoder.utils.MediaFormatExtraInfo;
import com.estsoft.muvigram.transcoder.utils.TranscodeUtils;

import java.nio.ByteBuffer;

/**
 * Created by estsoft on 2016-12-08.
 */

public class MediaTarget {
    private static final String TAG = "MediaTarget";
    public static final int VIDEO_ONLY = -101;
    public static final int AUDIO_ONLY = -102;
    public static final int NORMAL = -103;
    public static int CURRENT_MODE = 104;

//    MuxerWrapper muxerWrapper;
//    private final String outputFilePath;
//    private final int outputContainer = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;

    MediaFormat videoOutputFormat;
    private final String videoCodec = MediaFormatExtraInfo.MIMETYPE_VIDEO_AVC;
    private final int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

    MediaFormat audioOutputFormat;
    private final String audioCodec = MediaFormatExtraInfo.MIMETYPE_AUDIO_AAC;
    private final int audioAACProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    public MediaTarget() {

    }
//    public MediaTarget( MuxerWrapper wrapper, String outputFilePath ) {
//        this.muxerWrapper = wrapper;
//        this.outputFilePath = outputFilePath;
//    }
//
//    public MediaTarget(String outputFilePath, int mode) {
//        this.outputFilePath = outputFilePath;
//        MediaMuxer muxer;
//        try {
//            muxer = new MediaMuxer(this.outputFilePath, outputContainer);
//        } catch ( IOException e ) {
//            throw new IllegalStateException( e );
//        }
//        CURRENT_MODE = mode;
//        int muxerMode = -1;
//        switch ( mode ) {
//            case VIDEO_ONLY : muxerMode = MuxerWrapper.VIDEO_ONLY; break;
//            case AUDIO_ONLY : muxerMode = MuxerWrapper.AUDIO_ONLY; break;
//            case NORMAL : muxerMode = MuxerWrapper.NORMAL; break;
//            default : muxerMode = MuxerWrapper.NORMAL; break;
//        }
//        muxerWrapper = new MuxerWrapper( muxer, muxerMode );
//    }

    public void initVideoTarget(int interval, int frameRate, int bitrate, int rotation, int width, int height ) {
        videoOutputFormat = MediaFormat.createVideoFormat( videoCodec, width, height );
        videoOutputFormat.setInteger( MediaFormat.KEY_I_FRAME_INTERVAL, interval );
        videoOutputFormat.setInteger( MediaFormat.KEY_FRAME_RATE, frameRate );
        videoOutputFormat.setInteger( MediaFormat.KEY_BIT_RATE, bitrate );
        videoOutputFormat.setInteger( MediaFormat.KEY_COLOR_FORMAT, colorFormat );
        // NOTE Coded will select a profile on its own
//        videoOutputFormat.setInteger( MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline );
//        setCsdForVideo( videoOutputFormat );
    }
    private void setCsdForVideo( MediaFormat format ) {
        byte[] header_sps = { 0, 0, 0, 1, 103, 100, 0, 40, -84, 52, -59, 1, -32, 17, 31, 120, 11, 80, 16, 16, 31, 0, 0, 3, 3, -23, 0, 0, -22, 96, -108 };
        byte[] header_pps = { 0, 0, 0, 1, 104, -18, 60, -128 };
        format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
        format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        TranscodeUtils.printInformationOf( videoOutputFormat );
    }

    public void initAudioTarget( int sampleRate, int channelCount, int bitrate ) {
        audioOutputFormat = MediaFormat.createAudioFormat( audioCodec, sampleRate, channelCount );
        audioOutputFormat.setInteger( MediaFormat.KEY_AAC_PROFILE, audioAACProfile );
        audioOutputFormat.setInteger( MediaFormat.KEY_BIT_RATE, bitrate );
//        setCsdForAudio( audioOutputFormat, sampleRate, channelCount );
    }
    private void setCsdForAudio(MediaFormat format, int sampleRate, int channelCount ) {
        int samplingFreq[] = { // Android 참고 코드상 아래와 같은 samplerate를 지원
                96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
                16000, 12000, 11025, 8000
        };
        // Search the Sampling Frequencies
        // 아래 코드를 통해 0~11 에 맞는 값을 가져와야 합니다.
        // 일반적으로 44100을 사용하고 있으며, 여기에서는 4번에 해당됩니다.
        int sampleIndex = -1;
        for (int i = 0; i < samplingFreq.length; ++i) {
            if (samplingFreq[i] == sampleRate) {
                sampleIndex = i;
            }
        }
        /* 디코딩에 필요한 csd-0의 byte를 생성합니다. 이 부분은 Android 4.4.2의 Full source를 참고하여 작성
             * csd-0에서 필요한 byte는 2 byte 입니다. 2byte에 필요한 정보는 audio Profile 정보와
             * sample index, channelConfig 정보가 됩니다.
            */
        ByteBuffer csd = ByteBuffer.allocate(2);
        // 첫 1 byte에는 Audio Profile에 3 bit shift 처리합니다. 그리고 sample index를 1bit shift 합니다.
        csd.put((byte) ((audioAACProfile << 3) | (sampleIndex >> 1)));

        csd.position(1);
        // 다음 1 byte에는 sample index를 7bit shift 하고, channel 수를 3bit shift 처리합니다.
        csd.put((byte) ((byte) ((sampleIndex << 7) & 0x80) | (channelCount << 3)));
        csd.flip();
        // MediaCodec에서 필요하는 MediaFormat에 방금 생성한 "csd-0"을 저장합니다.
        format.setByteBuffer("csd-0", csd); // add csd-0
        TranscodeUtils.printInformationOf( audioOutputFormat );
    }

//    public void prepare() {
//        // TODO CSD configuration
//        muxerWrapper.setOutputFormat( MuxerWrapper.SampleType.VIDEO, videoOutputFormat );
//        muxerWrapper.setOutputFormat( MuxerWrapper.SampleType.AUDIO, audioOutputFormat );
//    }
//
//    public void stop() {
//        if ( !muxerWrapper.isStopped() ) muxerWrapper.stop();
//    }
//
//    public void release () {
//        if ( !muxerWrapper.isStopped() ) muxerWrapper.stop();
//        muxerWrapper.release();
//    }


}
