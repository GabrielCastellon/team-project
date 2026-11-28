package entity;

import java.util.List;

public class Board {
    private final String id;
    private final String userId;
    private final String name;
    private final List<String> pinIds;

    public Board(String id, String userId, String name, List<String> pinIds) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.pinIds = List.copyOf(pinIds);
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public List<String> getPinIds() {
        return pinIds;
    }
}
