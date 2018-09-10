package sergey.lavrenyuk.nn.score;

public interface RoundResultConsumer {

    void accept(Score.RoundResult roundResult);

    void close();
}
