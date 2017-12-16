package calculate;

import java.io.Serializable;

public class Request implements Serializable {
    private int level;
    private int way;

    public Request(int level, int way) {
        this.level = level;
        this.way = way;
    }

    public int getLevel() {
        return level;
    }

    public int getWay() {
        return way;
    }
}
