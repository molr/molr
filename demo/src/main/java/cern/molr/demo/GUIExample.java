package cern.molr.demo;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.mission.SequenceMissionExample;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class GUIExample {

    private JButton startButton;
    private JButton terminateButton;
    private JButton stepButton;
    private JButton skipButton;
    private JButton finishButton;

    private StringBuilder events = new StringBuilder();
    private StringBuilder states = new StringBuilder();
    private StringBuilder commandResponses = new StringBuilder();

    private MissionExecutionService service;

    public GUIExample(MissionExecutionService service) {
        Objects.requireNonNull(service);
        this.service = service;

        JFrame frame = new JFrame("Alignment Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JLabel eventsLabel = new JLabel("Events");
        JLabel eventsArea = new JLabel();
        JLabel statesLabel = new JLabel("States");
        JLabel statesArea = new JLabel();
        JButton instantiateButton = new JButton("INSTANTIATE");
        JLabel moleRunnerCommandsLabel = new JLabel("MoleRunner commands");
        startButton = new JButton("START");
        terminateButton = new JButton("TERMINATE");
        JLabel moleCommandsLabel = new JLabel("Sequence Mole commands");
        stepButton = new JButton("STEP");
        skipButton = new JButton("SKIP");
        finishButton = new JButton("FINISH");
        JLabel commandsResponsesLabel = new JLabel("Commands responses");
        JLabel commandsResponsesArea = new JLabel();

        startButton.setEnabled(false);
        terminateButton.setEnabled(false);
        stepButton.setEnabled(false);
        skipButton.setEnabled(false);
        finishButton.setEnabled(false);

        panel.add(instantiateButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(eventsLabel);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(eventsArea);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(statesLabel);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(statesArea);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(moleRunnerCommandsLabel);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(startButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(terminateButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(moleCommandsLabel);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(stepButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(skipButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(finishButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(commandsResponsesLabel);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(commandsResponsesArea);

        instantiateButton.addActionListener(e -> service.instantiate(SequenceMissionExample.class.getName(), null)
                .subscribe(new SimpleSubscriber<ClientMissionController>() {
            @Override
            public void consume(ClientMissionController controller) {
                controller.getEventsStream().subscribe(new SimpleSubscriber<MissionEvent>() {

                    @Override
                    public void consume(MissionEvent event) {
                        events.append(event).append("<br/>");
                        eventsArea.setText("<html>" + events + "</html>");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

                controller.getStatesStream().subscribe(new SimpleSubscriber<MissionState>() {

                    @Override
                    public void consume(MissionState state) {
                        states.append(state).append("<br/>");
                        statesArea.setText("<html>" + states + "</html>");
                        updateButtons(state);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

                startButton.addActionListener(e1 -> controller.instruct(new MissionControlCommand(MissionControlCommand.Command.START))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                    @Override
                    public void consume(CommandResponse response) {
                        commandResponses.append(response).append("<br/>");
                        commandsResponsesArea.setText("<html>" + commandResponses + "</html>");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));

                terminateButton.addActionListener(e1 -> controller.instruct(new MissionControlCommand(MissionControlCommand.Command
                        .TERMINATE))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                commandResponses.append(response).append("<br/>");
                                commandsResponsesArea.setText("<html>" + commandResponses + "</html>");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));

                stepButton.addActionListener(e1 -> controller.instruct(new SequenceCommand(SequenceCommand.Command.STEP))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                commandResponses.append(response).append("<br/>");
                                commandsResponsesArea.setText("<html>" + commandResponses + "</html>");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));

                skipButton.addActionListener(e1 -> controller.instruct(new SequenceCommand(SequenceCommand.Command.SKIP))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                commandResponses.append(response).append("<br/>");
                                commandsResponsesArea.setText("<html>" + commandResponses + "</html>");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));

                finishButton.addActionListener(e1 -> controller.instruct(new SequenceCommand(SequenceCommand.Command.FINISH))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                commandResponses.append(response).append("<br/>");
                                commandsResponsesArea.setText("<html>" + commandResponses + "</html>");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        }));



        frame.getContentPane().add(panel);

        frame.setSize(600, 600);
        frame.setVisible(true);
    }

    private void updateButtons(MissionState state) {
        switch (state.getLevel()) {
            case MOLE_RUNNER:
                startButton.setEnabled(false);
                terminateButton.setEnabled(false);
                for (MissionCommand command : state.getPossibleCommands()) {
                    if (command instanceof MissionControlCommand && ((MissionControlCommand) command).getCommand()
                            .equals(MissionControlCommand.Command.START)) {
                        startButton.setEnabled(true);
                    } else if (command instanceof MissionControlCommand && ((MissionControlCommand) command).getCommand()
                            .equals(MissionControlCommand.Command.TERMINATE)) {
                        terminateButton.setEnabled(true);
                    }
                }
                break;
            case MOLE:
                stepButton.setEnabled(false);
                skipButton.setEnabled(false);
                finishButton.setEnabled(false);
                for (MissionCommand command : state.getPossibleCommands()) {
                    if (command instanceof SequenceCommand && ((SequenceCommand) command).getCommand()
                            .equals(SequenceCommand.Command.STEP)) {
                        stepButton.setEnabled(true);
                    } else if (command instanceof SequenceCommand && ((SequenceCommand) command).getCommand()
                            .equals(SequenceCommand.Command.SKIP)) {
                        skipButton.setEnabled(true);
                    } else if (command instanceof SequenceCommand && ((SequenceCommand) command).getCommand()
                            .equals(SequenceCommand.Command.FINISH)) {
                        finishButton.setEnabled(true);
                    }
                }
                break;
        }
    }
}
