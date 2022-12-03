package hu.bme.mit.intellibus;

public class ACCController {

    final IEngineController engineController;

    // In this implementation, the states of the state chart are mapped to states represented by an enum
    public enum State {
        OFF, OPERATING
    }
    private int lastSpeed;
    private int lastDistance;
    private int accSpeed; // The target speed of the ACC to hold
    private int distanceThreshold;

    private State state = State.OFF;

    public ACCController(IEngineController engineController) {
        this.engineController = engineController;
        initializeValues();
    }

    public int getAccSpeed() {
        return accSpeed;
    }

    public State getState() {
        return state;
    }

    public void setSpeed(int value) {
        if(state == State.OPERATING)
            setAccSpeedAndSendSignals(value);
        else if(state == State.OFF) {
            state = State.OPERATING;
            accSpeed = value;
        }
    }

    public void currentSpeed(int value) {
        if(state==State.OPERATING) {
            lastSpeed = value;
            if(lastSpeed == accSpeed) engineController.holdSpeed();
            else if (lastSpeed < accSpeed) engineController.increaseSpeed();
            else if(lastSpeed > accSpeed) engineController.decreaseSpeed();
        }
    }

    public void currentDistance(int value) {
        setLastDistanceAndSendSignals(value);
    }

    public void turnOff() {
        if(state == State.OPERATING)
            state = State.OFF;
    }

    // Activities used by the state machine are mapped to private methods,
    // except for SendXYSignal activities, which are directly mapped to method calls,
    // and simple setter activities, which are mapped to assignments

    private void initializeValues() {
        lastSpeed = -1;
        lastDistance = -1;
        accSpeed = -1;
        distanceThreshold = 90;
    }

    private void setAccSpeedAndSendSignals(int currentSpeedValue) {
        lastSpeed = currentSpeedValue;
        if(accSpeed < lastSpeed) engineController.increaseSpeed();
        else if(accSpeed == lastSpeed) engineController.holdSpeed();
        else if(accSpeed > lastSpeed) engineController.decreaseSpeed();
    }

    private void setLastDistanceAndSendSignals(int currentDistanceValue) {
        lastDistance = currentDistanceValue;
        if(lastDistance < distanceThreshold) engineController.decreaseSpeed();
    }

}
