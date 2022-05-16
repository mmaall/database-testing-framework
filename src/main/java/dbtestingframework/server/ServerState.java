
package server;

enum Status {
    IDLE,
    DATA_GENERATING,
    DATA_GENERATED,
    LOAD_GENERATING,
    DONE
}

public class ServerState {

    private static ServerState state = new ServerState();

    public Status status;

    private ServerState() {
        status = Status.IDLE;
    }

    /* Static 'instance' method */
    public static ServerState getInstance( ) {
        return state;
    }

}