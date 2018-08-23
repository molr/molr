package cern.molr.sample.mission;

import cern.molr.commons.api.mission.RunWithMole;
import cern.molr.sample.mole.SequenceMission;
import cern.molr.sample.mole.SequenceMole;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A sequence mission example.
 *
 * @author yassine-kr
 */
@RunWithMole(SequenceMole.class)
public class SequenceMissionExample implements SequenceMission {


    @Override
    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(Task.of(() -> {
            try (PrintWriter out1 = new PrintWriter(new FileOutputStream("sequence-mission-example.txt"))) {
                out1.println("Task 1 begin");
                Thread.sleep(2000);
                out1.println("Task 1 end");
            } catch (FileNotFoundException | InterruptedException error1) {
                throw new RuntimeException(error1);
            }
        }, "task 1"));
        tasks.add(Task.of(() -> {
            try (PrintWriter out = new PrintWriter(new FileOutputStream("sequence-mission-example.txt", true))) {
                out.println("Task 2 begin");
                Thread.sleep(2000);
                out.println("Task 2 end");
            } catch (FileNotFoundException | InterruptedException error) {
                throw new RuntimeException(error);
            }
        }, "task 2"));
        tasks.add(Task.of(() -> {
            try (PrintWriter out = new PrintWriter(new FileOutputStream("sequence-mission-example.txt", true))) {
                out.println("Task 3 begin");
                Thread.sleep(2000);
                out.println("Task 3 end");
                throw new RuntimeException("Exception in task 3");
            } catch (FileNotFoundException | InterruptedException error) {
                throw new RuntimeException(error);
            }
        }, "task 3"));
        tasks.add(Task.of(() -> {
            try (PrintWriter out = new PrintWriter(new FileOutputStream("sequence-mission-example.txt", true))) {
                out.println("Task 4 begin");
                Thread.sleep(2000);
                out.println("Task 4 end");
            } catch (FileNotFoundException | InterruptedException error) {
                throw new RuntimeException(error);
            }
        }, "task 4") );
        return tasks;
    }

}
