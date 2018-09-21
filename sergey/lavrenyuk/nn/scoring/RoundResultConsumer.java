package sergey.lavrenyuk.nn.scoring;

public interface RoundResultConsumer {

    void accept(Score.RoundResult roundResult);

    void close();
}
