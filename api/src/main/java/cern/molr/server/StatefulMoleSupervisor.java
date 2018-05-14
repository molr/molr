package cern.molr.server;

import cern.molr.mole.supervisor.MoleSupervisor;

import java.util.Optional;

/**
 * It represents a supervisor which can tell whether it is idle or not
 *
 * @author yassine
 */
public interface StatefulMoleSupervisor extends MoleSupervisor {
    Optional<State> getState();

    /**
     * Supervisor state
     */
    class State{
        private boolean available;
        private int numMissions;

        public State() {
        }

        public State(boolean available, int numMissions) {
            this.available = available;
            this.numMissions = numMissions;
        }

        public boolean isAvailable() {
            return available;
        }

        public int isNumMissions() {
            return numMissions;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public void setNumMissions(int numMissions) {
            this.numMissions = numMissions;
        }
    }
}
