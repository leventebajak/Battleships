package leventebajak.battleships;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Testing the Game class.
 */
public class GameTest {

    File f1 = new File("test1");
    File f2 = new File("test2");
    File f3 = new File("test3");

    /**
     * Testing the save method.
     */
    @Before
    public void save() {
        assertFalse(f1.exists());
        assertFalse(f2.exists());
        assertFalse(f3.exists());
        new Game(false).save(f1);
        new Game(true).save(f2);
    }

    /**
     * Testing the load method.
     */
    @Test
    public void load() {
        try {
            Game.load(f1);
            Game.load(f2);
        } catch (IOException | ClassNotFoundException e) { fail(); }
        assertThrows(IOException.class, () -> Game.load(f3));
    }

    @After
    public void cleanup() {
        f1.delete();
        f2.delete();
    }
}