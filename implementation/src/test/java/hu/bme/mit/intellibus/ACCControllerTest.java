package hu.bme.mit.intellibus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ACCControllerTest {

    private MockEngineController mockEngineController;
    private ACCController acc;

    private static class MockEngineController implements IEngineController {
        enum State { INCREASE_SPEED, DECREASE_SPEED, TRAVELING_WITH_ACC_SPEED }

        private State state = State.TRAVELING_WITH_ACC_SPEED;
        @Override
        public void holdSpeed() {
            state = State.TRAVELING_WITH_ACC_SPEED;
        }

        @Override
        public void increaseSpeed() {
            state = State.INCREASE_SPEED;
        }

        @Override
        public void decreaseSpeed() {
            state = State.DECREASE_SPEED;
        }

        public State getState() {
            return state;
        }
    }

    @BeforeEach
    void setup() {
        // This test class will play the role of the TestConductor

        // The conductor fulfills the roles of the commander and the sensors.
        // All signals that the conductor sends to the ACC will be mapped to acc method calls

        // As it can be seen in the TestingSystem IBD, the AC Control under test is connected to
        // a mock engine controller, which will simply remember which signal was last sent to it,
        // and return the corresponding state when getState is called

        mockEngineController = new MockEngineController();
        acc = new ACCController(mockEngineController);
    }

    @Test
    void testReq1_1() {
        acc.setSpeed(50);

        // State invariants are mapped to assertions
        assertEquals(ACCController.State.OPERATING, acc.getState());
        assertEquals(acc.getAccSpeed(), 50);
    }

    //Tests whether the ACC turns off correctly
    @Test
    void testReq1_2(){
        acc.setSpeed(120);

        acc.turnOff();
        assertEquals(acc.getState(), ACCController.State.OFF);
    }

    @Test
    void testReq2_1_1_1() {
        acc.setSpeed(130);
        acc.currentSpeed(80);

        // As evaluate signals on the sequence diagrams are only used to provide ordering between otherwise unrelated
        // events, they are not mapped to method calls in the code, simply ignored

        assertEquals(MockEngineController.State.INCREASE_SPEED, mockEngineController.getState());
    }

    //tests whether the ACC reacts correctly to changed desired speed
    @Test
    void testReq1_3() {
        acc.setSpeed(130);
        acc.currentSpeed(80);
        assertEquals(MockEngineController.State.INCREASE_SPEED, mockEngineController.getState());
        acc.setSpeed(70);
        assertEquals(MockEngineController.State.DECREASE_SPEED, mockEngineController.getState());
    }

    //Tests whether tha acc can command the engine to hold speed.
    @Test
    void testReq2_1_1_2(){
        acc.setSpeed(90);
        acc.currentSpeed(90);

        assertEquals(MockEngineController.State.TRAVELING_WITH_ACC_SPEED, mockEngineController.getState());
    }

    //Tests whether the ACCs can handle down-slopes, or requests to reduce speed
    @Test
    void testReq2_1_1_3(){
        acc.setSpeed(50);
        acc.currentSpeed(75);
        assertEquals(MockEngineController.State.DECREASE_SPEED, mockEngineController.getState());
    }

    //Tests whether the ACC decreases the speed of the car correctly
    @Test
    void testReq2_2_1_1(){
        acc.setSpeed(50);
        acc.currentSpeed(45);
        assertEquals(MockEngineController.State.INCREASE_SPEED, mockEngineController.getState());

        acc.currentDistance(72);
        assertEquals(MockEngineController.State.DECREASE_SPEED, mockEngineController.getState());

    }

    //Tests whether the ACC may collide with an obstacle if its speed decreases.
    @Test
    void testReq2_2(){
        acc.setSpeed(50);
        acc.currentSpeed(45);
        acc.currentDistance(72);
        assertEquals(MockEngineController.State.DECREASE_SPEED, mockEngineController.getState());

        acc.currentSpeed(40);
        assertEquals(MockEngineController.State.DECREASE_SPEED, mockEngineController.getState());
    }


    //Tests whether the ACC increases speed correctly when the obstacle is farther than 90 meters.
    @Test
    void testReq2_2_1(){
        acc.setSpeed(50);
        acc.currentSpeed(45);
        acc.currentDistance(72);
        assertEquals(MockEngineController.State.DECREASE_SPEED, mockEngineController.getState());

        acc.currentDistance(91);
        assertEquals(MockEngineController.State.INCREASE_SPEED, mockEngineController.getState());
    }
}