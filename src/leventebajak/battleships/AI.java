package leventebajak.battleships;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Custom player, that can automatically make a guess based on previous guesses.
 */
public class AI extends Player implements Serializable {

    /**
     * The player to defeat.
     */
    public Player opponent;

    /**
     * The most recently guessed tile.
     */
    Tile lastGuess;

    /**
     * The first discovered tile of the ship.
     */
    Tile shipOrigin;

    /**
     * The different routes to check for ship tiles.
     */
    private ArrayList<Direction> possibleRoutes;

    /**
     * The route chosen from the possible routes.
     */
    Direction chosenRoute;

    /**
     * Creates an AI-controlled player.
     * @param opponent the player to defeat
     */
    AI(Player opponent) {
        super("Computer");
        this.opponent = opponent;
    }

    /**
     * Creates an AI-controlled player and places ships on its board.
     * @param opponent the player to defeat
     * @param shipLengths the lengths of the ships
     */
    AI(Player opponent, int... shipLengths) {
        super("Computer", shipLengths);
        this.opponent = opponent;
    }

    /**
     * Makes a guess based on the previous guesses.
     * @throws NullPointerException The AI has no opponent.
     */
    void makeAGuess() throws NullPointerException {
        if(opponent == null)
            throw new NullPointerException();

        // If we know about a ship, try to find its remaining tiles
        if (shipOrigin != null) {
            // If the chosen route is correct, continue guessing that way
            if (shipOrigin != lastGuess && lastGuess.isShip) {
                try {
                    Tile neighbor = lastGuess.getNeighbor(chosenRoute);
                    if (!neighbor.isDiscovered) {
                        neighbor.shoot();
                        lastGuess = neighbor;
                        return;
                    }
                    else {
                        chosenRoute = chosenRoute.opposite();
                        neighbor = shipOrigin.getNeighbor(chosenRoute);
                        if (!neighbor.isDiscovered) {
                            neighbor.shoot();
                            lastGuess = neighbor;
                            return;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    chosenRoute = chosenRoute.opposite();
                    Tile neighbor = shipOrigin.getNeighbor(chosenRoute);
                    if (!neighbor.isDiscovered) {
                        neighbor.shoot();
                        lastGuess = neighbor;
                        return;
                    }
                }
            }
            // Otherwise try finding the correct route
            else {
                while (possibleRoutes.size() > 0) {
                    chosenRoute = possibleRoutes.remove(RND.nextInt(0, possibleRoutes.size()));
                    try {
                        Tile neighbor = shipOrigin.getNeighbor(chosenRoute);
                        if (!neighbor.isDiscovered) {
                            neighbor.shoot();
                            lastGuess = neighbor;
                            return;
                        }
                    } catch (IndexOutOfBoundsException ignored) {}
                }
            }
            // If the whole ship has been discovered, forget it
            shipOrigin = null;
        }

        // Not knowing where to look, guess a tile randomly
        do {
            lastGuess = opponent.getTile(RND.nextInt(0, ROWS), RND.nextInt(0, COLUMNS));

            // If all the tile's neighbors are discovered, then don't guess that, as it cannot be a ship
            if (!lastGuess.isDiscovered) {
                try { if (!lastGuess.getNeighbor(Direction.NORTH).isDiscovered) continue;} catch (IndexOutOfBoundsException ignored) {}
                try { if (!lastGuess.getNeighbor(Direction.SOUTH).isDiscovered) continue;} catch (IndexOutOfBoundsException ignored) {}
                try { if (!lastGuess.getNeighbor(Direction.WEST).isDiscovered) continue;} catch (IndexOutOfBoundsException ignored) {}
                try { if (!lastGuess.getNeighbor(Direction.EAST).isDiscovered) continue;} catch (IndexOutOfBoundsException ignored) {}
                lastGuess = null;
            }
        } while (lastGuess == null || lastGuess.isDiscovered);

        // Finally, shoot the chosen tile
        lastGuess.shoot();
        // If it was part of a ship, remember it
        if (lastGuess.isShip) {
            shipOrigin = lastGuess;
            possibleRoutes = new ArrayList<>(Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
        }
    }
}
