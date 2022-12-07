package leventebajak.battleships;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Class representing players and their board.
 */
public class Player implements Serializable {
    protected static final Random RND = new Random();

    /**
     * The number of rows the board has.
     */
    public static int ROWS = 10;

    /**
     * The number of columns the board has.
     */
    public static int COLUMNS = 10;

    /**
     * The panel displaying the player's board.
     */
    transient public JPanel panel;

    /**
     * The player's name.
     */
    public String name;

    /**
     * Two-dimensional array representing the board.
     */
    private final ArrayList<ArrayList<Tile>> board = new ArrayList<>();

    /**
     * The tile of the board selected by the opponent.
     */
    public Tile selected;

    /**
     * The number of undiscovered ship tiles remaining on the board.
     */
    public int shipsRemaining = 0;

    /**
     * Creates a new player and places 5 ships on the player's board with the lengths of 5, 4, 3, 3 and 2.
     * @param name the name of the player
     */
    Player(String name) {
        this(name, 5, 4, 3, 3, 2);
    }

    /**
     * Creates a new player and places ships on the player's board.
     * @param name the name of the player
     * @param shipLengths the lengths of the ships
     */
    Player(String name, int... shipLengths) throws IllegalArgumentException {
        this.name = name;
        for (int row = 0; row < ROWS; row++) {
            board.add(row, new ArrayList<>());
            for (int column = 0; column < COLUMNS; column++)
                board.get(row).add(column, new Tile(this, row, column));
        }
        if (shipLengths != null)
            for (Integer length: shipLengths) {
                try {
                    addShip(length);
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (NoMoreSpaceException e) {
                    throw new RuntimeException();
                }
            }
        initialize();
    }

    /**
     * Initializes UI components.
     */
    public void initialize() {
        panel = new JPanel(new GridLayout(ROWS, COLUMNS));
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                panel.add(board.get(row).get(column).button);
            }
        }
        panel.setPreferredSize(new Dimension(COLUMNS * 50, ROWS * 50));
    }

    /**
     * Enables/Disables all tiles on the player's board.
     *
     * @param enabled whether the tiles should be enabled or disabled
     */
    public void setEnabled(boolean enabled) {
        for (int i = 0; i < ROWS * COLUMNS; i++)
            getTile(i / COLUMNS, i % COLUMNS).button.setEnabled(enabled);
    }

    /**
     * Reveals all tiles.
     */
    public void showAll() {
        for (int i = 0; i < ROWS * COLUMNS; i++)
            getTile(i / COLUMNS, i % COLUMNS).repaint(true);
    }

    /**
     * Shows only the discovered tiles and hides the rest.
     */
    public void showDiscovered() {
        for (int i = 0; i < ROWS * COLUMNS; i++)
            getTile(i / COLUMNS, i % COLUMNS).repaint(false);
    }

    /**
     * Gets a tile from the board.
     *
     * @param row    the row the tile lies in
     * @param column the column the tile lies in
     * @return the requested tile
     * @throws IndexOutOfBoundsException The requested tile falls outside the board.
     */
    public Tile getTile(int row, int column) throws IndexOutOfBoundsException {
        if (row < 0 || row >= ROWS || column < 0 || column >= COLUMNS)
            throw new IndexOutOfBoundsException();
        return board.get(row).get(column);
    }

    /**
     * Best-effort function for placing new ships on the board.
     * It's best to place the longer ships first and then the shorter ones.
     *
     * @param shipLength the length of the ship to be placed
     * @throws IllegalArgumentException The ship is too long to fit on the board, or has a length of less than two.
     * @throws NoMoreSpaceException There is not enough space left on the board to place the ship.
     */
    public void addShip(int shipLength) throws IllegalArgumentException, NoMoreSpaceException {
        // If the ship is too long to fit on the board, then don't even try placing it
        if (shipLength <= 1 || (shipLength > ROWS && shipLength > COLUMNS))
            throw new IllegalArgumentException();

        // Defining the maximum number of attempts to place the ship.
        final int max_attempts = 10000;
        int attempts = 0;
        HashSet<Tile> coordinateSet;

        finding_an_origin:
        while (true) {
            // Having reached the defined maximum number of attempts, give up trying to place the ship
            if (attempts++ == max_attempts)
                throw new NoMoreSpaceException();

            // Finding an origin point that has no neighboring ships
            Tile origin = getTile(RND.nextInt(0, ROWS), RND.nextInt(0, COLUMNS));

            // If the chosen origin point is not a valid ship placement, choose another one.
            if (!origin.validPlacement())
                continue finding_an_origin;

            // Defining possible routes
            ArrayList<Direction> possibleRoutes = new ArrayList<>(Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));

            Direction chosenRoute;
            Tile neighbor;

            choosing_route:
            while (true) {
                coordinateSet = new HashSet<>(Set.of(origin));

                try {
                    chosenRoute = possibleRoutes.remove(RND.nextInt(0, possibleRoutes.size()));
                } catch (IllegalArgumentException e) {
                    continue finding_an_origin;
                }

                try {
                    neighbor = origin.getNeighbor(chosenRoute);
                } catch (IndexOutOfBoundsException e) {
                    continue choosing_route;
                }

                placing_ship:
                while (true) {
                    // If the whole ship can be placed, the task has been completed
                    if (coordinateSet.size() == shipLength)
                        break finding_an_origin;

                    // If this is a valid placement, add the coordinate to the set
                    if (neighbor.validPlacement()) {
                        coordinateSet.add(neighbor);
                        if (coordinateSet.size() == shipLength)
                            break finding_an_origin;
                    }

                    // If the placement isn't valid, try going the opposite route
                    else if (possibleRoutes.remove(chosenRoute.opposite())) {
                        chosenRoute = chosenRoute.opposite();
                        try {
                            neighbor = origin.getNeighbor(chosenRoute);
                        } catch (IndexOutOfBoundsException e) {
                            continue choosing_route;
                        }
                        continue placing_ship;
                    }
                    else continue choosing_route;

                    // Otherwise continue going the chosen route
                    try {
                        neighbor = neighbor.getNeighbor(chosenRoute);
                    } catch (IndexOutOfBoundsException e) {
                        if (possibleRoutes.remove(chosenRoute.opposite())) {
                            chosenRoute = chosenRoute.opposite();
                            try {
                                neighbor = origin.getNeighbor(chosenRoute);
                            } catch (IndexOutOfBoundsException ignore) {
                                continue choosing_route;
                            }
                        } else continue choosing_route;
                    }
                }
            }
        }
        // Placing the ship on the board
        for (Tile tile : coordinateSet)
            tile.isShip = true;
        shipsRemaining += shipLength;
    }
}
