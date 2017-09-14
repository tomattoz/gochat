package red.tel.chat.network;


import red.tel.chat.VoipBackend;

public class NetworkIncomingCallProposal extends NetworkCallProposal {
    public NetworkIncomingCallProposal(NetworkCallProposalInfo callProposalInfo) {
        super(callProposalInfo);
    }

    @Override
    public void accept(NetworkCallProposalInfo callProposalInfo) {
        super.accept(callProposalInfo);
        VoipBackend.getInstance().sendCallAccept(callProposalInfo.from, callProposalInfo);
        NetworkIncomingCall.getInstance().start();
    }

    @Override
    public void decline() {
        VoipBackend.getInstance().sendCallDecline(callProposalInfo.from, callProposalInfo);
        super.decline();
    }

    @Override
    public void stop() {
        VoipBackend.getInstance().sendCallCancel(callProposalInfo.from, callProposalInfo);
        super.stop();
    }
}
