package ss.spec.client;

import java.util.List;
import java.util.Observer;

public interface SpecView extends Observer, Runnable {

    void promptGameRequest();

    void promptWaitingForGame(List<String> names);

    void closeView();

    void addChatMessage(String name, String message);
}
