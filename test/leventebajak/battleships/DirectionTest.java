package leventebajak.battleships;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testing the Direction enumeration.
 */
public class DirectionTest {

    /**
     * Testing the opposite method.
     */
    @Test
    public void oppositeTest() {
        assertEquals(Direction.SOUTH, Direction.NORTH.opposite());
        assertEquals(Direction.NORTH, Direction.SOUTH.opposite());
        assertEquals(Direction.WEST, Direction.EAST.opposite());
        assertEquals(Direction.EAST, Direction.WEST.opposite());
        assertEquals(Direction.NORTH_EAST, Direction.SOUTH_WEST.opposite());
        assertEquals(Direction.SOUTH_WEST, Direction.NORTH_EAST.opposite());
        assertEquals(Direction.NORTH_WEST, Direction.SOUTH_EAST.opposite());
        assertEquals(Direction.SOUTH_EAST, Direction.NORTH_WEST.opposite());
    }
}