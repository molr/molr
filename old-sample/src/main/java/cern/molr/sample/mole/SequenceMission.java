package cern.molr.sample.mole;

import java.util.List;
import java.util.Objects;

/**
 * A mission which is executed by a {@link SequenceMole}. A sequence mission is a simple list of tasks which are
 * executed consecutively by the Mole.
 *
 * @author yassine-kr
 */
public interface SequenceMission {

    List<Task> getTasks();

    public static class Task implements Runnable {
        private final Runnable delegate;
        private final String name;

        public Task(Runnable delegate, String name) {
            this.delegate = delegate;
            this.name = name;
        }

        public static Task of(Runnable task, String name)  {
            return new Task(task, name);
        }

        @Override
        public void run() {
            this.delegate.run();
        }

        public String name() {
            return this.name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Task task = (Task) o;
            return Objects.equals(delegate, task.delegate) &&
                    Objects.equals(name, task.name);
        }

        @Override
        public int hashCode() {

            return Objects.hash(delegate, name);
        }

        @Override
        public String toString() {
            return "Task{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

}
