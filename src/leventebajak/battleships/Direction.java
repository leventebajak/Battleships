package leventebajak.battleships;

import java.io.Serializable;

/**
 * Enumeration of the possible directions.
 */
public enum Direction implements Serializable {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST;

    /**
     * Gets the direction's opposite.
     * @return the opposite direction
     */
    Direction opposite() {
        return Direction.values()[(this.ordinal() + Direction.values().length/2) % Direction.values().length];
    }
}
