package red.tel.chat.network;


public class NetworkCallInfo {
    public NetworkCallProposalInfo proposal;
    public NetworkAudio.NetworkAudioSessionInfo audioSession;
    public NetworkVideo.NetworkVideoSessionInfo videoSession;

    public NetworkCallInfo(NetworkCallProposalInfo proposal,
                           NetworkAudio.NetworkAudioSessionInfo audioSession,
                           NetworkVideo.NetworkVideoSessionInfo videoSession) {
        this.proposal = proposal;
        this.audioSession = audioSession;
        this.videoSession = videoSession;
    }

    public NetworkCallInfo(NetworkCallProposalInfo proposal, NetworkAudio.NetworkAudioSessionInfo audioSession) {
        new NetworkCallInfo(proposal, audioSession, null);
    }

    public NetworkCallInfo(NetworkCallProposalInfo proposal, NetworkVideo.NetworkVideoSessionInfo videoSession) {
        new NetworkCallInfo(proposal, null, videoSession);
    }

    public NetworkCallInfo(NetworkCallProposalInfo proposal) {
        new NetworkCallInfo(this.proposal = proposal, null, null);
    }

    public String id() {
        return proposal.getId();
    }

    public String from() {
        return proposal.getFrom();
    }

    public String to() {
        return proposal.getTo();
    }
}
