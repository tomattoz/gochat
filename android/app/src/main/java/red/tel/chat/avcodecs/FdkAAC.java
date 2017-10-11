package red.tel.chat.avcodecs;

import java.util.HashMap;

public class FdkAAC {

    static {
        System.loadLibrary("native-lib");
    }

    private static int CODEC_BITRATE = 64000;

    public static final int AOT_AAC_LC = 2;
    public static final int AOT_HE_AAC = 5;
    public static final int AOT_AAC_LD = 23;
    public static final int AOT_AAC_ELD = 39;

    public static final int SAMPLING_RATE_48 = 48000;
    public static final int SAMPLING_RATE_44 = 44100;
    public static final int SAMPLING_RATE_32 = 32000;

    public static final int BITRATE_64 = 64000;
    public static final int BITRATE_48 = 48000;
    public static final int BITRATE_32 = 32000;
    public static final int BITRATE_24 = 24000;

    // AAC Profile
    private static class Profile {

        private int sampleRate;
        private int aot;
        private int frameSize;
        private boolean eldSBR = false;
        private String name;

        public int getSampleRate() {
            return sampleRate;
        }

        public int getAot() {
            return aot;
        }

        public int getFrameSize() {
            return frameSize;
        }

        public boolean isEldSBR() {
            return eldSBR;
        }

        public String getName() {
            return name;
        }


        public Profile(int sampleRate, int aot) {
            this.sampleRate = sampleRate;
            this.aot = aot;
            switch (this.aot) {
                case AOT_AAC_LD:
                    this.frameSize = 512;
                    this.name = "AAC-LD";
                    break;
                case AOT_AAC_ELD:
                    this.frameSize = 512;
                    this.name = "AAC-ELD";
                    break;
                case AOT_HE_AAC:
                    this.frameSize = 2048;
                    this.name = "HE-AAC";
                    break;
                default:
                    this.frameSize = 1024;
                    this.name = "AAC-LC";
            }
        }

        public Profile(int sampleRate, int aot, boolean eldSBR) {
            this(sampleRate, aot);
            this.eldSBR = eldSBR;
            if (this.aot == AOT_AAC_ELD && eldSBR) {
                this.frameSize = 1024;
                this.name += " (SBR)";
            }
        }
    }


    /**
     * Associates all supported profile configurations to their corresponding ASC
     */
    private static final HashMap<String, Profile> supportedProfiles = new HashMap<String, Profile>();

    static {
        // AAC-LC
        supportedProfiles.put("1188", new FdkAAC.Profile(48000, AOT_AAC_LC));
        supportedProfiles.put("1208", new FdkAAC.Profile(44100, AOT_AAC_LC));
        supportedProfiles.put("1288", new FdkAAC.Profile(32000, AOT_AAC_LC));

        // HE-AAC
        supportedProfiles.put("2B098800", new FdkAAC.Profile(48000, AOT_HE_AAC));
        supportedProfiles.put("2B8A0800", new FdkAAC.Profile(44100, AOT_HE_AAC));
        supportedProfiles.put("2C0A8800", new FdkAAC.Profile(32000, AOT_HE_AAC));

        // AAC-LD
        supportedProfiles.put("B98900", new FdkAAC.Profile(48000, AOT_AAC_LD));
        supportedProfiles.put("BA0900", new FdkAAC.Profile(44100, AOT_AAC_LD));
        supportedProfiles.put("BA8900", new FdkAAC.Profile(32000, AOT_AAC_LD));

        // AAC-ELD (with SBR)
        supportedProfiles.put("F8EC21ACE000", new FdkAAC.Profile(48000, AOT_AAC_ELD, true));
        supportedProfiles.put("F8EE21AF0000", new FdkAAC.Profile(44100, AOT_AAC_ELD, true));

        // AAC-ELD (without SBR)
        supportedProfiles.put("F8E62000", new FdkAAC.Profile(48000, AOT_AAC_ELD));
        supportedProfiles.put("F8E82000", new FdkAAC.Profile(44100, AOT_AAC_ELD));
        supportedProfiles.put("F8EA2000", new FdkAAC.Profile(32000, AOT_AAC_ELD));
    }

    public static Profile getProfile(String configCodec) {
        return supportedProfiles.get(configCodec);
    }

    private static byte[] getConfigBytes(String codecConfig) {
        int len = codecConfig.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(codecConfig.charAt(i), 16) << 4)
                    + Character.digit(codecConfig.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static boolean defaultOpen() {
        String configCode = "1208";
        Profile profile = getProfile(configCode);
        if (profile != null) {
            byte[] codec_config = getConfigBytes(configCode);
            int result = open(CODEC_BITRATE, profile.getSampleRate(), profile.getAot(), codec_config, codec_config.length, profile.isEldSBR());
            return result == 0;
        }
        return false;
    }

    private static native int open(int brate, int sample_rate, int aot, byte[] codec_config, int codec_config_length, boolean eldSBR);

    public native int encode(short[] lin, int offset, byte[] encoded, int size);

    public native int decode(byte[] encoded, short[] lin, int size);

    public native void close();
}
