package sergey.lavrenyuk.module;

import sergey.lavrenyuk.event.PerceptronEvent;

public interface PerceptronModule {

    void dispatch(PerceptronEvent event);
}
