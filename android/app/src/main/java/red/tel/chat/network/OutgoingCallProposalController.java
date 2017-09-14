package red.tel.chat.network;


public class OutgoingCallProposalController extends BaseCallProposalController {
    //static nested class
    private OutgoingCallProposalController() {
    }

    public static synchronized OutgoingCallProposalController getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public void start(NetworkCallProposalInfo callProposalInfo) {
        super.start(callProposalInfo);
    }

    private static class SingletonHelper {
        private static final OutgoingCallProposalController INSTANCE = new OutgoingCallProposalController();
    }
}
