package leventebajak.battleships;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

/**
 * Class representing tiles on players' boards.
 */
public class Tile implements Serializable {

    /**
     * The button associated with the tile.
     */
    transient public JButton button;

    /**
     * The color of the field selected by the opponent.
     */
    public static Color SELECTED_COLOR = Color.GREEN;

    /**
     * The color of the undiscovered tiles.
     */
    public static Color UNDISCOVERED_COLOR = Color.BLACK;

    /**
     * The color of the ships.
     */
    public static Color SHIP_COLOR = Color.GRAY;

    /**
     * The color of the water.
     */
    public static Color WATER_COLOR = Color.BLUE;

    /**
     * The player whose board this tile lies on.
     */
    private final Player owner;

    /**
     * The row of the board this tile lies in.
     */
    private final int row;

    /**
     * The column of the board this tile lies in.
     */
    private final int column;

    /**
     * Whether this tile is part of a ship.
     */
    public boolean isShip = false;

    /**
     * Whether this tile has been discovered by the opponent.
     */
    public boolean isDiscovered = false;

    /**
     * Creates a new tile.
     * @param owner the player whose board this tile lies on
     * @param row the row of the board this tile lies in
     * @param column the column of the board this tile lies in
     */
    Tile(Player owner, int row, int column) {
        this.owner = owner;
        this.row = row;
        this.column = column;
        initialize();
    }

    /**
     * Initializes UI components.
     */
    public void initialize() {
        button = new JButton();
        button.setBorder(new LineBorder(Color.BLACK));
        button.setForeground(Color.RED);
        button.setUI(new MetalButtonUI() { @Override protected Color getDisabledTextColor() { return Color.RED; } });
        button.addActionListener(ae -> {
            if(owner.selected != null)
                owner.selected.repaint(false);
            owner.selected = this;
            if (!this.isDiscovered)
                button.setBackground(SELECTED_COLOR);
            Game.endRoundButton.setEnabled(!this.isDiscovered);
        });
        button.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER)
                    Game.endRoundButton.doClick();
            }
        });
        if (isDiscovered)
            button.setText("X");
        repaint(false);
    }

    /**
     * Shoots this tile and reveals neighboring tiles according to the rules.
     */
    public void shoot() {
        if (this.isDiscovered)
            return;

        isDiscovered = true;
        owner.selected = null;
        button.setText("X");
        if (isShip)
            owner.shipsRemaining--;
        repaint(false);

        for (Direction d: new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            try {
                Tile neighbor = getNeighbor(d);
                if (isShip && neighbor.isDiscovered) {
                    if (neighbor.isShip) {
                        try { this.getNeighbor(Direction.values()[(d.ordinal() + 2) % Direction.values().length]).shoot(); } catch (IndexOutOfBoundsException ignored) {}
                        try { this.getNeighbor(Direction.values()[(d.ordinal() + 6) % Direction.values().length]).shoot(); } catch (IndexOutOfBoundsException ignored) {}
                    }
                    try { neighbor.getNeighbor(Direction.values()[(d.ordinal() + 2) % Direction.values().length]).shoot(); } catch (IndexOutOfBoundsException ignored) {}
                    try { neighbor.getNeighbor(Direction.values()[(d.ordinal() + 6) % Direction.values().length]).shoot(); } catch (IndexOutOfBoundsException ignored) {}
                }
                else if (!isShip && neighbor.isShip && neighbor.isDiscovered) {
                    try { this.getNeighbor(Direction.values()[(d.ordinal() + 2) % Direction.values().length]).shoot(); } catch (IndexOutOfBoundsException ignored) {}
                    try { this.getNeighbor(Direction.values()[(d.ordinal() + 6) % Direction.values().length]).shoot(); } catch (IndexOutOfBoundsException ignored) {}
                }
            } catch (IndexOutOfBoundsException ignored) {}
        }

        // If the whole ship has been sunk, reveal all its surrounding tiles
        checkSunken();
    }

    /**
     * If the whole ship has been sunk, reveals all its surrounding tiles.
     */
    public void checkSunken() {
        if (!isShip || !isDiscovered)
            return;

        for (Direction d: Direction.values()) {
            try {
                Tile neighbor = getNeighbor(d);
                if (neighbor.isShip) {
                    if (!neighbor.isDiscovered)
                        return;
                    while (true) {
                        try {
                            neighbor = neighbor.getNeighbor(d);
                            if (neighbor.isShip && !neighbor.isDiscovered)
                                return;
                            if (!neighbor.isShip)
                                break;
                        } catch (IndexOutOfBoundsException e) { break; }
                    }
                    while (true) {
                        try {
                            neighbor = neighbor.getNeighbor(d.opposite());
                            if (neighbor.isShip && !neighbor.isDiscovered)
                                return;
                            if (!neighbor.isShip)
                                break;
                        } catch (IndexOutOfBoundsException e) { break; }
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {}
        }
        for (Direction d: Direction.values()) {
            try {
                Tile neighbor = getNeighbor(d);
                neighbor.shoot();
                while (neighbor.isShip) {
                    for (Direction dir : Direction.values())
                        try {
                            neighbor.getNeighbor(dir).shoot();
                        } catch (IndexOutOfBoundsException ignored) {}
                    neighbor = neighbor.getNeighbor(d);
                }
            } catch (IndexOutOfBoundsException ignored) {}
        }
    }

    /**
     * Get the tile's neighbor in a direction.
     * @param direction direction of the required neighbor
     * @return the neighbor tile
     * @throws IndexOutOfBoundsException There is no tile in the given direction.
     */
    Tile getNeighbor(Direction direction) throws IndexOutOfBoundsException {
        return switch (direction) {
            case NORTH -> owner.getTile(row - 1, column);
            case SOUTH -> owner.getTile(row + 1, column);
            case WEST  -> owner.getTile(row, column - 1);
            case EAST  -> owner.getTile(row, column + 1);
            case NORTH_WEST -> owner.getTile(row - 1, column - 1);
            case NORTH_EAST -> owner.getTile(row - 1, column + 1);
            case SOUTH_WEST -> owner.getTile(row + 1, column - 1);
            case SOUTH_EAST -> owner.getTile(row + 1, column + 1);
        };
    }

    /**
     * Determines whether a ship can be placed on this tile, meaning that it
     * doesn't already have any neighboring ships, and it is not a ship either.
     * @return whether a ship can be placed on the tile
     */
    boolean validPlacement() {
        if (this.isShip)
            return false;

       for (Direction d: Direction.values())
           try {
               if (getNeighbor(d).isShip)
                   return false;
           } catch (IndexOutOfBoundsException ignored) {}
       return true;
    }


    /**
     * Updates the color of the tile.
     * @param reveal whether to reveal the tile if it is undiscovered
     */
    public void repaint(boolean reveal) {
        if (reveal)
            button.setBackground(isShip ? SHIP_COLOR : WATER_COLOR);
        else
            button.setBackground(isDiscovered ? isShip ? SHIP_COLOR : WATER_COLOR : UNDISCOVERED_COLOR);
        button.repaint();
    }
}
