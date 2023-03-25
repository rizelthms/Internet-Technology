package Server.ServerThread;

import Shared.Printer;
import Shared.Protocol;

class PingPongThread extends Thread {
    private final ServerThread parent;
    //Flag to indicate if a PONG message has been received from the client
    private boolean hasPonged = false;
    private boolean isRunning = true;

    public PingPongThread(ServerThread parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                parent.getWriter().println(Protocol.PING);
                // Sleep for 3 seconds
                Thread.sleep(3000);

                if (!hasPonged) {
                    parent.getWriter().println(Protocol.DISCONNECT + " Pong timeout");
                    parent.getWriter().println(Protocol.FAIL05);

                    parent.stopThread();
                } else {
                    //Pong received, reset flag
                    hasPonged = false;
                }
            } catch (Exception e) {
                Printer.printLineColour(e.getMessage(), Printer.ConsoleColour.RED);

                parent.stopThread();
            }
        }
    }

    public void setHasPonged(boolean hasPonged) {
        this.hasPonged = hasPonged;
    }

    public void stopThread() {
        this.isRunning = false;
    }
}
