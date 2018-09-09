package sergey.lavrenyuk.nn;

public interface RoundResultConsumer {

    void accept(Score.RoundResult roundResult);

    void close();
}
