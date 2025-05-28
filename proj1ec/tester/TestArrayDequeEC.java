package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;


public class TestArrayDequeEC {

    @Test
    public void randomTest() {
        StudentArrayDeque<Integer> studentDeque = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> solutionDeque = new ArrayDequeSolution<>();

        String operationsLog = "";
        int testOperations = 500;

        for (int i = 0; i < testOperations; i++) {
            double randomChoice = StdRandom.uniform();
            if (randomChoice < 0.25) {
                int randomVal = StdRandom.uniform(1000);
                studentDeque.addFirst(randomVal);
                solutionDeque.addFirst(randomVal);
                operationsLog += "addFirst(" + randomVal + ")\n";
                assertEquals(operationsLog.toString(), solutionDeque.size(), studentDeque.size());
            } else if (randomChoice < 0.5) {
                int randomVal = StdRandom.uniform(1000);
                studentDeque.addLast(randomVal);
                solutionDeque.addLast(randomVal);
                operationsLog += "addLast(" + randomVal + ")\n";             
                assertEquals(operationsLog.toString(), solutionDeque.size(), studentDeque.size());
            } else if (randomChoice < 0.75) {
                if (!solutionDeque.isEmpty() && !studentDeque.isEmpty()) {
                    Integer expectedValue = solutionDeque.removeFirst();
                    Integer actualValue = studentDeque.removeFirst();
                    operationsLog += "removeFirst()\n";         
                    assertEquals(operationsLog.toString(), expectedValue, actualValue);
                }
            } else {
                if (!solutionDeque.isEmpty() && !studentDeque.isEmpty()) {
                    Integer expectedValue = solutionDeque.removeLast();
                    Integer actualValue = studentDeque.removeLast();
                    operationsLog += "removeLast()\n";      
                    assertEquals(operationsLog.toString(), expectedValue, actualValue);
                }
            }
        }
    }
}
