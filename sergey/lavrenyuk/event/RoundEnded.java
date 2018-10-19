package sergey.lavrenyuk.event;

public class RoundEnded implements PerceptronEvent {

    private final boolean victory;

    public RoundEnded(boolean victory) {
        this.victory = victory;
    }

    public boolean isVictory() {
        return victory;
    }
}
