package leventebajak.battleships;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * The class that controls and displays the game.
 */
public class Game implements Serializable {

    /**
     * The file, that the game is saved to on exit.
     */
    public static File EXIT_SAVE = new File("last.save");

    /**
     * The button the players can use to end their turn.
     */
    public static JButton endRoundButton;

    /**
     * The frame the players' boards are displayed in.
     */
    transient public JFrame frame;

    /**
     * The player whose turn is active.
     */
    private Player activePlayer;

    /**
     * The computer, or the player who is waiting.
     */
    private Player inactivePlayer;

    /**
     * Whether the opponent is another human.
     */
    private final boolean pvp;

    /**
     * Whether the game has ended.
     */
    private boolean over = false;

    /**
     * Create a new game.
     * @param pvp whether the opponent is another human
     */
    Game(boolean pvp) {
        this.pvp = pvp;
        if (pvp) {
            activePlayer = new Player("PLAYER 1");
            inactivePlayer = new Player("PLAYER 2");
        }
        else {
            activePlayer = new Player("YOU");
            inactivePlayer = new AI(activePlayer);
        }
        initialize();
    }

    /**
     * Initializes UI components.
     */
    public void initialize() {
        Main.FRAME.setVisible(false);
        frame = new JFrame("BATTLESHIPS");
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu newGameMenu = new JMenu("New game");
        JMenuItem mainMenu = new JMenuItem("Main menu");
        JMenuItem saveGame = new JMenuItem("Save game");
        JMenuItem loadGame = new JMenuItem("Load game");
        JMenuItem vsComputer = new JMenuItem("VS computer");
        JMenuItem vsPlayer = new JMenuItem("VS player");
        fileMenu.add(mainMenu);
        fileMenu.add(saveGame);
        fileMenu.add(loadGame);
        newGameMenu.add(vsComputer);
        newGameMenu.add(vsPlayer);
        menuBar.add(fileMenu);
        menuBar.add(newGameMenu);
        frame.setJMenuBar(menuBar);

        mainMenu.addActionListener(ae -> {
            closeGame();
            Main.FRAME.setVisible(true);
        });
        saveGame.addActionListener(ae -> {
            JFileChooser fc = new JFileChooser(new File("").getAbsolutePath());
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
                save(fc.getSelectedFile());
        });
        loadGame.addActionListener(ae -> {
            if (Main.loadGame() == JFileChooser.APPROVE_OPTION) {
                closeGame();
                Main.FRAME.setVisible(false);
            }
        });
        vsComputer.addActionListener(ae -> {
            closeGame();
            Main.vsComputer();
        });
        vsPlayer.addActionListener(ae -> {
            closeGame();
            Main.vsPlayer();
        });

        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Main.FRAME.getContentPane().getBackground());

        activePlayer.setEnabled(false);

        JLabel player1_name = new JLabel(activePlayer.name, SwingConstants.CENTER);
        JLabel player2_name = new JLabel(inactivePlayer.name, SwingConstants.CENTER);

        frame.setFont(new Font("Stencil", Font.BOLD, 50));
        player1_name.setFont(frame.getFont());
        player2_name.setFont(frame.getFont());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(player1_name, BorderLayout.NORTH);
        leftPanel.add(activePlayer.panel, BorderLayout.SOUTH);
        leftPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        leftPanel.setBackground(Main.FRAME.getContentPane().getBackground());
        frame.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(player2_name, BorderLayout.NORTH);
        rightPanel.add(inactivePlayer.panel, BorderLayout.SOUTH);
        rightPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        rightPanel.setBackground(Main.FRAME.getContentPane().getBackground());
        frame.add(rightPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        endRoundButton = new JButton("End round");
        endRoundButton.addActionListener(ae -> nextRound());
        endRoundButton.setEnabled(inactivePlayer.selected != null && !inactivePlayer.selected.isDiscovered);
        endRoundButton.setPreferredSize(new Dimension(400, 100));
        endRoundButton.setFont(frame.getFont());
        endRoundButton.setBackground(Main.FRAME.getContentPane().getBackground());
        endRoundButton.setBorder(null);
        bottomPanel.add(endRoundButton);
        bottomPanel.setBackground(Main.FRAME.getContentPane().getBackground());
        frame.add(bottomPanel, BorderLayout.SOUTH);
        if (pvp)
            JOptionPane.showMessageDialog(null, activePlayer.name + "\nPress OK to start!");
        activePlayer.showAll();
        inactivePlayer.showDiscovered();
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!over) save(EXIT_SAVE);
                frame.dispose();
                Main.continueButton.setEnabled(EXIT_SAVE.exists());
            }
        });
    }

    /**
     * This is what happens when a player ends the turn.
     */
    public void nextRound() {
        // Shoot and reveal the selected tile on the opponent's board
        inactivePlayer.selected.shoot();
        inactivePlayer.showDiscovered();
        frame.repaint();
        if (pvp) inactivePlayer.setEnabled(false);

        // If the opponent has no more ships, the current player won
        if (inactivePlayer.shipsRemaining == 0) {
            inactivePlayer.showAll();
            frame.repaint();
            JOptionPane.showMessageDialog(null, activePlayer.name + " won!");
            over = true;
        }
        else {
            // Otherwise it's the other player's round...
            if (pvp) {
                Player tmp = activePlayer;
                activePlayer = inactivePlayer;
                inactivePlayer = tmp;

                activePlayer.showDiscovered();
                inactivePlayer.showDiscovered();
                frame.repaint();
                JOptionPane.showMessageDialog(null, activePlayer.name + "\n Press OK to continue!");
                activePlayer.showAll();
                frame.repaint();

                inactivePlayer.setEnabled(true);
            }
            // ...or the computer makes a guess
            else {
                ((AI) inactivePlayer).makeAGuess();
                // If the player has no ships left, the computer won
                if (activePlayer.shipsRemaining == 0) {
                    inactivePlayer.setEnabled(false);
                    inactivePlayer.showAll();
                    frame.repaint();
                    JOptionPane.showMessageDialog(null, "You lost!");
                    over = true;
                }
            }
        }
        endRoundButton.setEnabled(false);
    }

    /**
     * Save the game to the given file.
     * @param file where the game will be saved
     */
    public void save(File file) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(this);
            out.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Tries loading the game using from the given file.
     * @param file where the game is saved
     * @return the loaded game
     * @throws IOException Something is wrong with a class used by deserialization.
     * @throws ClassNotFoundException Class of a serialized object cannot be found.
     */
    public static Game load(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        Game result = (Game) in.readObject();
        in.close();
        for (int row = 0; row < Player.ROWS; row++) {
            for (int column = 0; column < Player.COLUMNS; column++) {
                result.activePlayer.getTile(row, column).initialize();
                result.inactivePlayer.getTile(row, column).initialize();
            }
        }
        result.activePlayer.initialize();
        result.inactivePlayer.initialize();
        result.initialize();
        return result;
    }

    /**
     * Closes the window, thus ending the game.
     */
    public void closeGame() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }
}
