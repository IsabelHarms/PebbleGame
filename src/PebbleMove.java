public class PebbleMove {
    public enum Action { PLACE, REMOVE }
    public Action action;
    public State state;
    public int time;

    public PebbleMove(Action action, State state, int time) {
        this.action = action;
        this.state = state;
        this.time = time;
    }

    @Override
    public String toString() {
        return time + ": " + action + " pebble on " + state;
    }
}
