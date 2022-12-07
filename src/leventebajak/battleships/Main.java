package leventebajak.battleships;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * Main class, that builds the main menu.
 */
public class Main {

    /**
     * This frame is the main menu.
     */
    static JFrame FRAME = new JFrame("BATTLESHIPS");

    /**
     * This button loads the last game. It is only enabled, if the file exists.
     */
    static JButton continueButton;

    public static void main(String[] args) {
        FRAME.setLayout(new BorderLayout());
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FRAME.setPreferredSize(new Dimension(1280, 720));
        FRAME.getContentPane().setBackground(Color.WHITE);

        JPanel title_panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("BATTLESHIPS", SwingConstants.CENTER);
        title.setFont(new Font("Stencil", Font.BOLD, 100));
        title_panel.add(title);
        title_panel.setPreferredSize(new Dimension(title.getWidth(), 200));
        title_panel.setBackground(FRAME.getContentPane().getBackground());
        FRAME.add(title_panel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(5,1));
        buttons.setFont(new Font("Stencil", Font.BOLD, 50));

        // This button loads the last game. It is only enabled, if the file exists.
        continueButton = new JButton("Continue last game");
        continueButton.addActionListener(ae -> continueGame());
        continueButton.setEnabled(Game.EXIT_SAVE.exists());
        continueButton.setFont(buttons.getFont());
        continueButton.setBackground(FRAME.getContentPane().getBackground());
        continueButton.setBorder(null);
        buttons.add(continueButton);

        // This button starts a new game against the computer.
        JButton vsComputerButton = new JButton("New game vs computer");
        vsComputerButton.addActionListener(ae -> vsComputer());
        vsComputerButton.setFont(buttons.getFont());
        vsComputerButton.setBackground(FRAME.getContentPane().getBackground());
        vsComputerButton.setBorder(null);
        buttons.add(vsComputerButton);

        // This button starts a new game against another player.
        JButton vsPlayerButton = new JButton("New game vs player");
        vsPlayerButton.addActionListener(ae -> vsPlayer());
        vsPlayerButton.setFont(buttons.getFont());
        vsPlayerButton.setBackground(FRAME.getContentPane().getBackground());
        vsPlayerButton.setBorder(null);
        buttons.add(vsPlayerButton);

        // This button allows the user to load a previously saved game.
        JButton load_game = new JButton("Load saved game");
        load_game.addActionListener(ae -> loadGame());
        load_game.setFont(buttons.getFont());
        load_game.setBackground(FRAME.getContentPane().getBackground());
        load_game.setBorder(null);
        buttons.add(load_game);

        // This button closes the game.
        JButton exit_game = new JButton("Exit game");
        exit_game.addActionListener( ae -> System.exit(0));
        exit_game.setFont(buttons.getFont());
        exit_game.setBackground(FRAME.getContentPane().getBackground());
        exit_game.setBorder(null);
        buttons.add(exit_game);

        FRAME.add(buttons);
        FRAME.pack();
        FRAME.setLocationRelativeTo(null);
        FRAME.setVisible(true);
    }

    /**
     * Displays the game window and hides the main menu.
     * @param game the Game to start
     */
    public static void startGame(Game game) {
        game.frame.setVisible(true);
        game.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Main.FRAME.setVisible(true);
            }
        });
    }

    /**
     * Tries loading the last game.
     */
    public static void continueGame() {
        try {
            startGame(Game.load(Game.EXIT_SAVE));
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "The file could not be loaded.");
        }
    }

    /**
     * Pops up an "Open File" file chooser dialog and tries loading the selected file.
     * @return the return state of the file chooser
     */
    public static int loadGame() {
        JFileChooser fc = new JFileChooser(new File("").getAbsolutePath());
        int result = fc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                startGame(Game.load(file));
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "The file could not be loaded.");
            }
        }
        return result;
    }

    /**
     * Starts a new game against the computer.
     */
    public static void vsComputer() {
        startGame(new Game(false));
    }

    /**
     * Starts a new game against another player.
     */
    public static void vsPlayer() {
        startGame(new Game(true));
    }
}