package ss.spec.client;

import java.util.Observer;

public interface SpecView extends Observer, Runnable {

    void promptGameRequest();

    void closeView();

    void addChatMessage(String name, String message);
}
