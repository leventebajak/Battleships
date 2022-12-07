package leventebajak.battleships;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testing the AI class.
 */
public class AITest {

    /**
     * Testing the makeAGuess method.
     */
    @Test(timeout = 1000)
    public void makeAGuess() {
        assertThrows(NullPointerException.class, () -> new AI(null).makeAGuess());

        Player p = new Player("test_player", 2);
        try {
            p.addShip(2);
        } catch (NoMoreSpaceException e) { fail(); }
        AI ai = new AI(p, (int[]) null);
        while (ai.shipOrigin == null)
            ai.makeAGuess();
        Tile ship = ai.shipOrigin;

        int guesses = 0;
        while (true) {
            if (guesses > 4)
                fail();
            ai.makeAGuess();
            Tile guess = ai.lastGuess;
            if (guess.isShip) {
                assertEquals(ship, guess.getNeighbor(ai.chosenRoute.opposite()));
                break;
            }
            guesses++;
        }
    }
}