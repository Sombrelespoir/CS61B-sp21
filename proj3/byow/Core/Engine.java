package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;

    private World world;
    private long SEED = 0L;
    private String COMMAND = "";


    private boolean isPlaying = false;
    private boolean hasCommand = false;
    private boolean needToLoad = false;
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        analyseInput(input);

        if (needToLoad) {
            load();
        } else {
            world = new World(WIDTH, HEIGHT, SEED);
            isPlaying = true;
        }
        if (hasCommand) {
            movePlayer();
        }

        return world.getTiles();
    }

    public void analyseInput(String input) {
        if (input.isEmpty()) {
            return;
        }
        if (input.charAt(0) == 'n' || input.charAt(0) == 'N') {
            int sPosition = -1;
            for (int i = 1; i < input.length(); i++) {
                if (input.charAt(i) == 'S' || input.charAt(i) == 's') {
                    sPosition = i;
                    break;
                }
            }

            StringBuilder number = new StringBuilder();
            for (int i = 1; i < sPosition; i++) {
                char c = input.charAt(i);
                number.append(c);
            }

            SEED = Long.parseLong(number.toString());
            input = input.substring(sPosition + 1);
        } else if (input.charAt(0) == 'l' || input.charAt(0) == 'L') {
            needToLoad = true;
            input = input.substring(1);
        }

        StringBuilder commandString = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case 's', 'S':
                    commandString.append('s');
                    break;
                case 'w', 'W':
                    commandString.append('w');
                    break;
                case 'a', 'A':
                    commandString.append('a');
                    break;
                case 'd', 'D':
                    commandString.append('d');
                    break;
                case ':':
                    if (i + 1 < input.length() && (input.charAt(i + 1) == 'q' || input.charAt(i + 1) == 'Q')) {
                        isPlaying = false;
                        saveWorld();
                        i++;
                    }
                    break;
            }
        }
        COMMAND = commandString.toString();
        hasCommand = !COMMAND.isEmpty();
    }

    public void load() {

    }

    public void movePlayer() {

    }

    public void saveWorld() {

    }


}
