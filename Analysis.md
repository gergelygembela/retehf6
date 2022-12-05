# Verification & Validation

## Reviewing requirements

### Questions

> 1\. The user shall be able to turn on the ACC

1. *How to turn on the ACC?*

2. *How is the user notified when the ACC is turned on?*

> 2\. When turned on the ACC shall maintain the target speed set by the driver.

1. *How does the ACC maintain the set target speed?*

> 3\. If the current speed is lower than the target speed and ACC does not detect a car in front in 90 meters, then the ACC shall command the engine to accelerate.

1. *How does the ACC detect a car in front of 90 meters?*

> 4\. If ACC detects a car in front, then it shall decrease the speed of the car.

1. *How does the ACC detect a car in front?*


### Comments

The 3rd and 4th requirements both cover car detection, but only a part of the expected outcome.

Requirements 3 and 4 are both potentially derived requirements.

Requirements 2 and 4 are contradictory when a car is in front.

A high-level requirement of car detection shall be defined.

As for requirement 4, it should be more abstract, like holding a distance of at least 90 meters.

### Proposed requirements

1\. The user shall be able to command the ACC

1\.1\. The user shall be able to set a target speed and use that to engage ACC if it is off.

1\.2\. The user shall be able to clear the set speed and thereby turn off the ACC.

2\. The ACC shall command the engine control when it is on.

2\.1\. The ACC shall maintain the target speed set by the driver.

2\.1\.1\. The ACC shall command the engine control according to the difference between the target speed and the current speed.

2\.1\.1\.1\. If the current speed is lower than the target speed, then the ACC shall command the engine to accelerate.

2\.1\.1\.2\. If the current speed is equal to the target speed, then the ACC shall command the engine to hold speed.

2\.1\.1\.3\. If the current speed is higher than the target speed, then the ACC shall command the engine to decelerate.

2\.2\. The ACC shall hold a distance of at least 90 meters ahead.

2\.2\.1\. The ACC must determine if there is a car within 90 meters ahead.

2\.2\.1\.1\. If ACC detects a car in front, then it shall command the engine to decelerate.

## Reviewing implementation

### Comments on ACCController.java

`ACCController.lastSpeed` is never used.
`ACCController.lastDistance` is never used.

`ACCController.setAccSpeedAndSendSignals(int)` and `ACCController.setLastDistanceAndSendSignals(int)` shall be combined into `ACCController.updateStateAndSendSignals(int, int)`, whereas `ACCController.lastDistance` and `ACCController.lastSpeed` shall be used as parameters if either it is not the upgradable value.

### Potential implementation

``` Java
void setAccSpeedAndSendSignals(int currentSpeedValue) {
    updateStateAndSendSignals(currentSpeedValue, lastDistance);
}
```
``` Java
void setLastDistanceAndSendSignals(int currentDistanceValue) {
    updateStateAndSendSignals(lastSpeed, currentDistanceValue);
}
```
``` Java
void updateStateAndSendSignals(int speed, int distance) {
    lastSpeed = speed;
    lastDistance = distance;
    if (lastDistance < distanceThreshold)
        engineController.decreaseSpeed();
    else {
        if (accSpeed < lastSpeed)
            engineController.increaseSpeed();
        else if (accSpeed == lastSpeed)
            engineController.holdSpeed();
        else if (accSpeed > lastSpeed)
            engineController.decreaseSpeed();
    }
}
```


## Test objectives

### Test testReq1_1
Tests whether the ACC can be turned on.

### Test testReq1_2
Tests whether the ACC can be turned off.

### Test testReq2_1_1_1
Tests whether the ACC can command the engine to increase the speed of the car correctly.

### Test testReq2_1_1_2
Tests whether the ACC can command the engine to hold/maintain speed.

### Test testReq2_1_1_3
Tests whether the ACC can command the engine to decrease the speed.

### Test testReq2_2_1_1
Tests whether the ACC decreases the speed of the car correctly.

### Test testReq2_2
Tests whether the ACC may collide with an obstacle if its speed decreases.

### Test testReq2_2_1
Tests whether the ACC increases speed correctly when the obstacle is farther than 90 meters.

## Review and summary

We defined more test cases after inspecting the provided implementation, 
as we found that it presents dangerous and erroneous behavior because the system fails to 
take both parameters into account when commanding the EngineController.
The new tests fail, even though they only evaluate expected behavior, and for the system to be able to operate (more) safely, the problems described above should be fixed, and the additional tests we defined must all perform successfully.
