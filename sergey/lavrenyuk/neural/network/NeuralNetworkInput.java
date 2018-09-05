package sergey.lavrenyuk.neural.network;

public class NeuralNetworkInput {

    private final double myEnergy;
    private final double myX;
    private final double myY;
    private final double myXMoveVector;
    private final double myYMoveVector;

    private final double enemyEnergy;
    private final double enemyX;
    private final double enemyY;
    private final double enemyXMoveVector;
    private final double enemyYMoveVector;

    public NeuralNetworkInput(double myEnergy, double myX, double myY, double myXMoveVector, double myYMoveVector,
                              double enemyEnergy, double enemyX, double enemyY, double enemyXMoveVector, double enemyYMoveVector) {
        this.myEnergy = myEnergy;
        this.myX = myX;
        this.myY = myY;
        this.myXMoveVector = myXMoveVector;
        this.myYMoveVector = myYMoveVector;
        this.enemyEnergy = enemyEnergy;
        this.enemyX = enemyX;
        this.enemyY = enemyY;
        this.enemyXMoveVector = enemyXMoveVector;
        this.enemyYMoveVector = enemyYMoveVector;
    }

    public double getMyEnergy() {
        return myEnergy;
    }

    public double getMyX() {
        return myX;
    }

    public double getMyY() {
        return myY;
    }

    public double getMyXMoveVector() {
        return myXMoveVector;
    }

    public double getMyYMoveVector() {
        return myYMoveVector;
    }

    public double getEnemyEnergy() {
        return enemyEnergy;
    }

    public double getEnemyX() {
        return enemyX;
    }

    public double getEnemyY() {
        return enemyY;
    }

    public double getEnemyXMoveVector() {
        return enemyXMoveVector;
    }

    public double getEnemyYMoveVector() {
        return enemyYMoveVector;
    }
}
