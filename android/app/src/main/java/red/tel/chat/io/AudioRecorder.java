package red.tel.chat.io;


import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class AudioRecorder {
    public static class AudioFormat {
        public AudioFormat() {
        }

        private static String kFormatID = "kFormatID";
        private static String kFlags = "kFlags";
        private static String kSampleRate = "kSampleRate";
        private static String kChannelCount = "kChannelCount";
        private static String kFramesPerPacket = "kFramesPerPacket";

        private IO.IOFormat format;

    }

    private Thread mAudioThread;
    private AudioRecordRunnable mAudioRecordRunnable;
    private boolean runAudioThread = false;
    private AudioRecord mAudioRecord;
    private int mFrameSize;
    private int mFilterLength;
    private int mBufferSize;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    public static final int SAMPLE_RATE = 44100; // Hz
    public static final int ENCODING = android.media.AudioFormat.ENCODING_PCM_16BIT;
    public static final int CHANNEL_MASK = android.media.AudioFormat.CHANNEL_IN_MONO;


    public static final int BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MASK, ENCODING);

    public static final  int BufferElements2Rec = 1024 * 2; //want to play 2048 (2K) since 2 bytes we use only 1024

    private AudioRecord createAudioRecorder() {

       /* int BytesPerElement = 2; // 2 bytes in 16bit format*/

        AudioRecord record = new AudioRecord(AUDIO_SOURCE,
                SAMPLE_RATE, CHANNEL_MASK,
                ENCODING, BUFFER_SIZE);
        return record;
    }


    public interface AudioRecorderListener {
        void onAudioDataUpdate(ByteBuffer buffer, ShortBuffer[] samples, byte[] data);

        void onFail();
        void onAudioDataUpdate(byte[] data);
    }

    public AudioRecord getAudioRecord() {
        return mAudioRecord;
    }

    public AudioRecorder() {
      /*  float samplesPerMilli = (float) SAMPLE_RATE / 1000.0f;

        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_MASK,
                ENCODING);

        mFrameSize = (int) (40.0f * samplesPerMilli);
        if (mFrameSize * 2 > minBufferSize) {
            mBufferSize = mFrameSize * 2;
        } else {
            mBufferSize = minBufferSize;
            mFrameSize = minBufferSize / 2;
        }
        mFilterLength = (int) (200.0f * samplesPerMilli);*/

    }

    public void startRecorder(AudioRecorderListener audioRecorderListener) {
        mAudioRecordRunnable = new AudioRecordRunnable(audioRecorderListener);
        mAudioThread = new Thread(mAudioRecordRunnable);
        runAudioThread = true;
        mAudioThread.start();
    }

    public void release() {
        runAudioThread = false;
        try {
            mAudioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAudioRecordRunnable = null;
        mAudioThread = null;
    }

    private class AudioRecordRunnable implements Runnable {
        private AudioRecorderListener mAudioRecorderListener;

        AudioRecordRunnable(AudioRecorderListener audioRecorderListener) {
            mAudioRecorderListener = audioRecorderListener;
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
           /* mAudioRecord = new AudioRecord(
                    AUDIO_SOURCE,
                    SAMPLE_RATE,
                    CHANNEL_MASK,
                    ENCODING,
                    BUFFER_SIZE);*/

           mAudioRecord = createAudioRecorder();


        }

/*        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            // Audio
            byte[] byteAudioData = new byte[BUFFER_SIZE];
            ByteBuffer audioData;
            int bufferReadResult;
            int read;
            audioData = ByteBuffer.allocateDirect(mBufferSize);
            mAudioRecord.startRecording();
            ShortBuffer[] samples = new ShortBuffer[10 * SAMPLE_RATE * 2 / mBufferSize + 1];
            for (int i = 0; i < samples.length; i++) {
                samples[i] = ShortBuffer.allocate(mBufferSize);
            }
            while (runAudioThread) {
                if (mAudioRecord != null) {
                    bufferReadResult = mAudioRecord.read(audioData, mBufferSize);
                    read = mAudioRecord.read(byteAudioData, 0, byteAudioData.length);
                    if (bufferReadResult == AudioRecord.ERROR_INVALID_OPERATION || bufferReadResult == AudioRecord.ERROR_BAD_VALUE) {
                        mAudioRecorderListener.onFail();
                    } else {
                        if (bufferReadResult > 0) {
                            mAudioRecorderListener.onAudioDataUpdate(audioData, samples, byteAudioData);
                        }
                    }
                }
            }
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }*/

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            // Audio
           mAudioRecord.startRecording();
           byte data[] = new byte[BUFFER_SIZE];
            while (runAudioThread) {
                if (mAudioRecord != null) {
                    mAudioRecord.read(data, 0, BUFFER_SIZE);
                    mAudioRecorderListener.onAudioDataUpdate(data);
                }
            }
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }
    }

}
