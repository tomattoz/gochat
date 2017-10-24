package red.tel.chat.io;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import red.tel.chat.avcodecs.FdkAAC;

public class AudioRecorder {

    /**
     * prvs
     */
    private Thread mAudioThread;
    private AudioRecordRunnable mAudioRecordRunnable;
    private boolean runAudioThread = false;
    private AudioRecord mAudioRecord;

    /**
     * consts
     */
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    public static final int SAMPLE_RATE = 44100; // Hz
    public static final int ENCODING = android.media.AudioFormat.ENCODING_PCM_FLOAT;
    public static final int CHANNEL_MASK = android.media.AudioFormat.CHANNEL_IN_MONO;
    public static final int BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MASK, ENCODING);

    /**
     *
     * @return AudioRecord
     */
    private AudioRecord createAudioRecorder() {

        // AAC engineer init
        FdkAAC.shared().defaultOpen();

        // Return
        return new AudioRecord(AUDIO_SOURCE,
                SAMPLE_RATE, CHANNEL_MASK,
                ENCODING, FdkAAC.shared().expectedEncodingInBufferSize());
    }

    /**
     * AudioRecorderListener interface
     */
    public interface AudioRecorderListener {

        /**
         *
         * @param data The encoded data
         */
        void onAudioDataUpdate(byte[] data);
    }

    /**
     *
     * @return audio record
     */
    public AudioRecord getAudioRecord() {
        return mAudioRecord;
    }

    /**
     * ctor
     */
    public AudioRecorder() {
        // TODO
    }

    /**
     *
     * @param audioRecorderListener The listener
     */
    public void startRecorder(AudioRecorderListener audioRecorderListener) {
        mAudioRecordRunnable = new AudioRecordRunnable(audioRecorderListener);
        mAudioThread = new Thread(mAudioRecordRunnable);
        runAudioThread = true;
        mAudioThread.start();
    }

    /**
     * release
     */
    public void release() {

        // mask audio thread false
        runAudioThread = false;

        // try/catch
        try {
            mAudioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // dealloc things
        mAudioRecordRunnable = null;
        mAudioThread = null;
    }

    /**
     * A runnable
     */
    private class AudioRecordRunnable implements Runnable {
        private AudioRecorderListener mAudioRecorderListener;

        /**
         * ctor
         * @param audioRecorderListener The audio listener
         */
        AudioRecordRunnable(AudioRecorderListener audioRecorderListener) {
            mAudioRecorderListener = audioRecorderListener;
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
            mAudioRecord = createAudioRecorder();


        }

        /**
         * Override run method, it starts audio-recording, capture PCM data and encode AAC
         */
        @Override
        public void run() {

            // Thread priority
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Record starting
            mAudioRecord.startRecording();

            // Sizes of buffers
            int sizeBufferIN = FdkAAC.shared().expectedEncodingInBufferSize();
            int sizeBufferOUT = FdkAAC.shared().expectedEncodingOutBufferSize();

            // Buffer to read
            short data[] = new short[sizeBufferIN];

            // Buffer to encode
            byte encodedBuffer[] = new byte[sizeBufferOUT];

            // Loop to read
            while (runAudioThread) {

                // Sure available recording
                if (mAudioRecord != null) {

                    // Read audio buffer in short
                    int read = mAudioRecord.read(data, 0, sizeBufferIN);

                    // AAC encode
                    int encodedLen = FdkAAC.shared().encode(data, 0, encodedBuffer, read);
                    byte encoded[] = Arrays.copyOfRange(encodedBuffer,0, encodedLen);

                    // Fallback encoded data to listener
                    mAudioRecorderListener.onAudioDataUpdate(encoded);
                }
            }

            // End recording
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }
    }

}
