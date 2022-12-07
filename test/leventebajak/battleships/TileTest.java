package leventebajak.battleships;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testing the Tile class.
 */
public class TileTest {

    /**
     * Testing the colors.
     */
    @Test
    public void repaint() {
        Tile t = new Tile(null, 0, 0);
        assertEquals(t.button.getBackground(), Tile.UNDISCOVERED_COLOR);
        assertFalse(t.isDiscovered);
        t.repaint(false);
        assertEquals(t.button.getBackground(), Tile.UNDISCOVERED_COLOR);
        t.isDiscovered = true;
        t.repaint(false);
        assertEquals(t.button.getBackground(), Tile.WATER_COLOR);
        assertFalse(t.isShip);
        t.isShip = true;
        t.repaint(false);
        assertEquals(t.button.getBackground(), Tile.SHIP_COLOR);
        t.isDiscovered = false;
        t.repaint(true);
        assertEquals(t.button.getBackground(), Tile.SHIP_COLOR);
    }

    /**
     * Testing the shoot method.
     */
    @Test
    public void shoot() {
        Tile t = new Tile(new Player("test", (int[]) null), 0, 0);
        assertFalse(t.isDiscovered);
        assertEquals(t.button.getText(), "");
        t.shoot();
        assertTrue(t.isDiscovered);
        assertEquals(t.button.getText(), "X");
        t.shoot();
        assertTrue(t.isDiscovered);
        assertEquals(t.button.getText(), "X");
    }

    /**
     * Testing the getNeighbor method.
     */
    @Test
    public void getNeighbor() {
        Player p = new Player("test", (int[]) null);
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(0, 0).getNeighbor(Direction.WEST));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(0, 0).getNeighbor(Direction.NORTH));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(Player.ROWS-1, Player.COLUMNS-1).getNeighbor(Direction.EAST));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(Player.ROWS-1, Player.COLUMNS-1).getNeighbor(Direction.SOUTH));
        for (Direction direction: Direction.values()) {
            try {
                p.getTile(Player.ROWS/2, Player.COLUMNS/2).getNeighbor(direction);
            } catch (IndexOutOfBoundsException e) { fail(); }
        }
    }

    /**
     * Testing the validPlacement method.
     */
    @Test
    public void validPlacement() {
        // On an empty board, every tile should be a valid ship placement
        Player p = new Player("test", (int[]) null);
        for (int row = 0; row < Player.ROWS; row++)
            for (int column = 0; column < Player.COLUMNS; column++)
                for (Direction direction: Direction.values())
                    try {
                        assertTrue(p.getTile(row, column).getNeighbor(direction).validPlacement());
                    } catch (IndexOutOfBoundsException ignored) {}
        // Placing a ship on the board
        try {
            p.addShip(2);
        } catch (NoMoreSpaceException e) {
            fail();
        }
        // Now the ships' and their neighbors' tiles cannot be valid placements anymore
        for (int row = 0; row < Player.ROWS; row++)
            for (int column = 0; column < Player.COLUMNS; column++) {
                Tile t = p.getTile(row, column);
                if (t.isShip) {
                    assertFalse(t.validPlacement());
                    for (Direction direction : Direction.values())
                        try {
                            assertFalse(t.getNeighbor(direction).validPlacement());
                        } catch (IndexOutOfBoundsException ignored) {}
                }
            }
    }
}