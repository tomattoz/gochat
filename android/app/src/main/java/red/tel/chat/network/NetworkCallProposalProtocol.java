package red.tel.chat.network;


import red.tel.chat.Types;

public interface NetworkCallProposalProtocol extends Types.SessionProtocol {
    void accept(NetworkCallProposalInfo info);

    void decline();
}
