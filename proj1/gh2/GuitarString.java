package gh2;

import deque.ArrayDeque;
import deque.Deque;


//Note: This file will not compile until you complete the Deque implementations
public class GuitarString {
   
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        int capacity = (int) Math.round(SR / frequency);
        buffer = new ArrayDeque<>();
        for (int i = 0; i < capacity; i++) {
            buffer.addLast(0.0);
        }
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {

        int size = buffer.size();
        while (!buffer.isEmpty()) {
            buffer.removeFirst();
        }
        for (int i = 0; i < size; i++) {
            double r = Math.random() - 0.5;
            buffer.addLast(r);
        }
    }


    public void tic() {

        double firstSample = buffer.removeFirst();
        double secondSample = buffer.get(0);
        double newSample = DECAY * 0.5 * (firstSample + secondSample);
        buffer.addLast(newSample);

    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        return buffer.get(0);
    }
}

