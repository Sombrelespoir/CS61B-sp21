package capers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static capers.Utils.*;


public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File CAPERS_FOLDER = Utils.join(CWD, ".capers");
    static final File DOGS_FOLDER = Utils.join(CAPERS_FOLDER, "dogs");
    static final File STORY_FILE = Utils.join(CAPERS_FOLDER, "story");

    /**
     * does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * remember: recommended structure (you do not have to follow):
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all of the persistent data for dogs
     *    - story -- file containing the current story
     */
    public static void setupPersistence() throws IOException {
        if (!CAPERS_FOLDER.exists()) {
            CAPERS_FOLDER.mkdirs();
        }
        if (!DOGS_FOLDER.exists()) {
            DOGS_FOLDER.mkdirs();
        }
        if (!STORY_FILE.exists()) {
            STORY_FILE.createNewFile();
        }
    }
    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) throws IOException {
        setupPersistence();
        String oldStory = "";
        if (STORY_FILE.exists()) {
            oldStory = readContentsAsString(STORY_FILE);
        }
        String newStory = oldStory.isEmpty() ? text : oldStory + "\n" + text;
        writeContents(STORY_FILE, newStory);
        System.out.println(newStory);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) throws IOException {
        setupPersistence();
        Dog dog = new Dog(name, breed, age);
        File dogFile = Utils.join(DOGS_FOLDER, name);
        Utils.writeObject(dogFile, dog);
        System.out.println(dog.toString());
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) throws IOException {
        setupPersistence();
        File dogFile = Utils.join(DOGS_FOLDER, name);
        if (!dogFile.exists()) {
            System.out.println("The dog does not exist.");
            return;
        }
        Dog dog = Utils.readObject(dogFile, Dog.class);
        dog.haveBirthday();
        Utils.writeObject(dogFile, dog);
    }
}
