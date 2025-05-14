package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correctList = new AListNoResizing<>();
        BuggyAList<Integer> buggyAList = new BuggyAList<>();
        correctList.addLast(4);
        buggyAList.addLast(4);
        correctList.addLast(5);
        buggyAList.addLast(5);
        correctList.addLast(6);
        buggyAList.addLast(6);
        boolean check1 = (Objects.equals(correctList.removeLast(), buggyAList.removeLast()));
        boolean check2 = (Objects.equals(correctList.removeLast(), buggyAList.removeLast()));
        boolean check3 = (Objects.equals(correctList.removeLast(), buggyAList.removeLast()));

    }
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();

        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 2);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);

            } else if (operationNumber == 1) {
                // size
                int size = L.size();

            }
        }
    }
}