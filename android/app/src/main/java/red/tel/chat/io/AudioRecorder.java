package red.tel.chat.io;


import android.media.AudioFormat;
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

    private int mSampleRate = 44100;
    private Thread mAudioThread;
    private AudioRecordRunnable mAudioRecordRunnable;
    private boolean runAudioThread = false;
    private AudioRecord mAudioRecord;
    private int mFrameSize;
    private int mFilterLength;
    private int mBufferSize;

    public interface AudioRecorderListener {
        void onAudioDataUpdate(ByteBuffer buffer, ShortBuffer[] samples);

        void onFail();
    }

    public AudioRecord getAudioRecord() {
        return mAudioRecord;
    }

    public AudioRecorder() {
        float samplesPerMilli = (float) mSampleRate / 1000.0f;

        int minBufferSize = AudioRecord.getMinBufferSize(
                mSampleRate,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT);

        mFrameSize = (int) (40.0f * samplesPerMilli);
        if (mFrameSize * 2 > minBufferSize) {
            mBufferSize = mFrameSize * 2;
        } else {
            mBufferSize = minBufferSize;
            mFrameSize = minBufferSize / 2;
        }
        mFilterLength = (int) (200.0f * samplesPerMilli);
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
            mAudioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    mSampleRate,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    mBufferSize);
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            // Audio
            ByteBuffer audioData;
            int bufferReadResult;
            audioData = ByteBuffer.allocateDirect(mBufferSize);
            mAudioRecord.startRecording();
            ShortBuffer[] samples = new ShortBuffer[10 * mSampleRate * 2 / mBufferSize + 1];
            for (int i = 0; i < samples.length; i++) {
                samples[i] = ShortBuffer.allocate(mBufferSize);
            }
            while (runAudioThread) {
                if (mAudioRecord != null) {
                    bufferReadResult = mAudioRecord.read(audioData, mBufferSize);
                    if (bufferReadResult == AudioRecord.ERROR_INVALID_OPERATION || bufferReadResult == AudioRecord.ERROR_BAD_VALUE) {
                        mAudioRecorderListener.onFail();
                    } else {
                        if (bufferReadResult > 0) {
                            mAudioRecorderListener.onAudioDataUpdate(audioData, samples);
                        }
                    }
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
