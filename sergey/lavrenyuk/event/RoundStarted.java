package sergey.lavrenyuk.event;

import sergey.lavrenyuk.module.PerceptronControl;

public class RoundStarted implements PerceptronEvent{

    private final PerceptronControl robotControl;

    public RoundStarted(PerceptronControl robotControl) {
        this.robotControl = robotControl;
    }

    public PerceptronControl getRobotControl() {
        return robotControl;
    }
}
