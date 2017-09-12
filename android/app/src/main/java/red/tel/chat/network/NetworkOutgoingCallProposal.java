package red.tel.chat.network;


import red.tel.chat.VoipBackend;

public class NetworkOutgoingCallProposal extends NetworkCall.NetworkCallProposal {
    public NetworkOutgoingCallProposal(NetworkCall.NetworkCallProposalInfo info) {
        super(info);
    }

    @Override
    public void start() {
        super.start();
        VoipBackend.getInstance().sendCallProposal(info.to, info);
    }

    @Override
    public void stop() {
        VoipBackend.getInstance().sendCallCancel(info.to, info);
        super.stop();
    }

    @Override
    public void accept(NetworkCall.NetworkCallProposalInfo info) {
        VoipBackend.getInstance().sendCallAccept(info.from, info);
        super.accept(info);
    }

    @Override
    public void decline() {
        VoipBackend.getInstance().sendCallDecline(info.from, info);
        super.decline();
    }
}
