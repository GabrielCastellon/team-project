package entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardFactoryTest {
    private final BoardFactory factory = new BoardFactory();

    @Test
    void createsEmptyBoardWithGivenOwnerAndName() {
        Board board = factory.create("u17", "Gym");

        assertEquals("u17", board.getUserId());
        assertEquals("Gym", board.getName());
        assertTrue(board.getPinIds().isEmpty());
        assertFalse(board.getId().isBlank());
    }

    @Test
    void givesEachBoardItsOwnId() {
        Board gym = factory.create("u17", "Gym");
        Board recipes = factory.create("u17", "Recipes");

        assertNotEquals(gym.getId(), recipes.getId());
    }
}
