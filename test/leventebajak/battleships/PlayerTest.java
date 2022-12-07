package leventebajak.battleships;

import jdk.jfr.Timespan;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testing the Player class.
 */
public class PlayerTest {

    /**
     * Testing the setEnabled method.
     */
    @Test
    public void setEnabled() {
        Player p = new Player("test", (int[]) null);
        for (int i = 0; i < Player.ROWS * Player.COLUMNS; i++)
            assertTrue(p.getTile(i / Player.COLUMNS, i % Player.COLUMNS).button.isEnabled());
        p.setEnabled(false);
        for (int i = 0; i < Player.ROWS * Player.COLUMNS; i++)
            assertFalse(p.getTile(i / Player.COLUMNS, i % Player.COLUMNS).button.isEnabled());
        p.setEnabled(true);
        for (int i = 0; i < Player.ROWS * Player.COLUMNS; i++)
            assertTrue(p.getTile(i / Player.COLUMNS, i % Player.COLUMNS).button.isEnabled());
    }

    /**
     * Testing the showAll method.
     */
    @Test
    public void showAll() {
        Player p = new Player("test", (int[]) null);
        for (int i = 0; i < Player.ROWS * Player.COLUMNS; i++) {
            Tile t = p.getTile(i / Player.COLUMNS, i % Player.COLUMNS);
            assertEquals(t.button.getBackground(), t.isDiscovered ? t.isShip ? Tile.SHIP_COLOR : Tile.WATER_COLOR : Tile.UNDISCOVERED_COLOR);
        }
        p.showAll();
        for (int i = 0; i < Player.ROWS * Player.COLUMNS; i++) {
            Tile t = p.getTile(i / Player.COLUMNS, i % Player.COLUMNS);
            assertEquals(t.button.getBackground(), t.isShip ? Tile.SHIP_COLOR : Tile.WATER_COLOR);
        }
    }

    /**
     * Testing the showDiscovered method.
     */
    @Test
    public void showDiscovered() {
        Player p = new Player("test", (int[]) null);
        for (int i = 0; i < Player.ROWS * Player.COLUMNS; i++) {
            Tile t = p.getTile(i / Player.COLUMNS, i % Player.COLUMNS);
            assertEquals(t.button.getBackground(), t.isDiscovered ? t.isShip ? Tile.SHIP_COLOR : Tile.WATER_COLOR : Tile.UNDISCOVERED_COLOR);
        }
        p.showDiscovered();
        for (int i = 0; i < Player.ROWS * Player.COLUMNS; i++) {
            Tile t = p.getTile(i / Player.COLUMNS, i % Player.COLUMNS);
            assertEquals(t.button.getBackground(), t.isDiscovered ? t.isShip ? Tile.SHIP_COLOR : Tile.WATER_COLOR : Tile.UNDISCOVERED_COLOR);
        }
    }

    /**
     * Testing the getTile method.
     */
    @Test
    public void getTile() {
        Player p = new Player("test", (int[]) null);
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(0, Player.COLUMNS));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getTile(Player.ROWS, 0));
        assertEquals(p.getTile(0, 0), p.getTile(0, 1).getNeighbor(Direction.WEST));
        assertEquals(p.getTile(0, 0), p.getTile(1, 0).getNeighbor(Direction.NORTH));
        assertEquals(p.getTile(0, 0), p.getTile(1, 1).getNeighbor(Direction.NORTH_WEST));
    }

    /**
     * Testing the addShip method.
     */
    @Test(timeout = 100)
    public void addShip() {
        Player p = new Player("test", (int[]) null);
        assertThrows(IllegalArgumentException.class, () -> p.addShip(-1));
        assertThrows(IllegalArgumentException.class, () -> p.addShip(0));
        assertThrows(IllegalArgumentException.class, () -> p.addShip(1));
        assertThrows(IllegalArgumentException.class, () -> p.addShip(Player.ROWS + 1));
        assertThrows(IllegalArgumentException.class, () -> p.addShip(Player.COLUMNS + 1));

        // Placing ships according to the game rules
        try {
            p.addShip(5);
            assertEquals(5, p.shipsRemaining);
            p.addShip(4);
            assertEquals(5+4, p.shipsRemaining);
            p.addShip(3);
            assertEquals(5+4+3, p.shipsRemaining);
            p.addShip(3);
            assertEquals(5+4+3+3, p.shipsRemaining);
            p.addShip(2);
            assertEquals(5+4+3+3+2, p.shipsRemaining);
        } catch (NoMoreSpaceException e) { fail(); }

        // Repeatedly adding ships until there is no more space left
        try {
            while (true)
                p.addShip(Player.ROWS/2);
        } catch (NoMoreSpaceException ignored) {}

        // Testing constructors
        try {
            Player p1 = new Player("test", 5, 4, 3, 3, 2);
            Player p2 = new Player("test");
        } catch (IllegalArgumentException e) { fail(); }

    }
}