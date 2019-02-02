package ss.spec.client;

import java.util.Observer;

public interface SpecView extends Observer, Runnable {

    void closeView();
}
