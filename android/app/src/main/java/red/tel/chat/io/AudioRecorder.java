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

    // Internal class
    public static class AudioFormat {

        private static String kFormatID = "kFormatID";
        private static String kFlags = "kFlags";
        private static String kSampleRate = "kSampleRate";
        private static String kChannelCount = "kChannelCount";
        private static String kFramesPerPacket = "kFramesPerPacket";

        private IO.IOFormat format;

        public AudioFormat() {
            format = new IO.IOFormat();
        }

        public Integer getFormatID() {
            return (Integer) format.getData().get(kFormatID);
        }

        public void setFormatID(Integer formatID) {
            format.getData().put(kFormatID, formatID);
        }

        public Integer getFlags() {
            return (Integer) format.getData().get(kFormatID);
        }

        public void setFlags(Integer flags) {
            format.getData().put(kFlags, flags);
        }

        public Double getSampleRate() {
            return (Double) format.getData().get(kFormatID);
        }

        public void setSampleRate(Double sampleRate) {
            format.getData().put(kSampleRate, sampleRate);
        }

        public Integer getChannelCount() {
            return (Integer) format.getData().get(kChannelCount);
        }

        public void setChannelCount(Integer channelCount) {
            format.getData().put(kChannelCount, channelCount);
        }

        public Integer getFramesPerPacket() {
            return (Integer) format.getData().get(kFramesPerPacket);
        }

        public void setFramesPerPacket(Integer framesPerPacket) {
            format.getData().put(kFramesPerPacket, framesPerPacket);
        }

        public byte[] toNetwork() {
            JSONObject json = new JSONObject(format.getData());
            return json.toString().getBytes();
        }

        public void fromNetwork(byte[] remoteData) {
            String str = new String(remoteData);

            try {
                JSONObject jObject = new JSONObject(str);
                this.setFormatID((Integer) jObject.get(kFormatID));
                this.setFlags((Integer) jObject.get(kFlags));
                this.setSampleRate((Double) jObject.get(kSampleRate));
                this.setChannelCount((Integer) jObject.get(kChannelCount));
                this.setFramesPerPacket((Integer) jObject.get(kFramesPerPacket));
            } catch (Exception e) {

            }

        }
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


    private AudioRecord createAudioRecorder() {

       /* int BytesPerElement = 2; // 2 bytes in 16bit format*/

        FdkAAC.shared().defaultOpen();
        AudioRecord record = new AudioRecord(AUDIO_SOURCE,
                SAMPLE_RATE, CHANNEL_MASK,
                ENCODING, FdkAAC.shared().expectedEncodingInBufferSize());

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
        // TODO
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
            mAudioRecord = createAudioRecorder();


        }


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
