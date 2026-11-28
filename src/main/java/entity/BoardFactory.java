package entity;

import java.util.List;
import java.util.UUID; // Universally Unique Identifier for generating ID

public class BoardFactory {
    public Board create(String userId, String name) {
        String boardId = UUID.randomUUID().toString(); // Generate ID
        return new Board(boardId, userId, name, List.of()); // List.of() since board is empty
    }
}
