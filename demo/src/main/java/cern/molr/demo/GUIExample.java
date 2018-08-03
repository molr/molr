package cern.molr.demo;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionRunnerEvent;
import cern.molr.commons.events.MissionExceptionEvent;
import cern.molr.commons.events.MissionFinished;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.events.SequenceMissionEvent;
import cern.molr.sample.mission.SequenceMissionExample;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Objects;

/**
 * GUI example
 *
 * @author yassine-kr
 */
public class GUIExample {

    private JButton startButton;
    private JButton terminateButton;
    private JButton stepButton;
    private JButton skipButton;
    private JButton resumeButton;
    private JButton pauseButton;

    private DefaultListModel<String> eventsListModel = new DefaultListModel<>();
    private DefaultListModel<String> statesListModel = new DefaultListModel<>();
    private DefaultListModel<String> commandsResponsesListModel = new DefaultListModel<>();

    private JList<String> eventsList;
    private JList<String> statesList;
    private JList<String> commandResponsesList;

    private MissionExecutionService service;

    public GUIExample(MissionExecutionService service) {
        Objects.requireNonNull(service);
        this.service = service;

        JFrame frame = new JFrame("Sequence Mole Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel horizontalPanel = new JPanel();
        horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.LINE_AXIS));
        horizontalPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel commandsPanel = new JPanel();
        commandsPanel.setLayout(new BoxLayout(commandsPanel, BoxLayout.PAGE_AXIS));
        JButton instantiateButton = new JButton("INSTANTIATE");
        JLabel moleRunnerCommandsLabel = new JLabel("<html><h3><strong><i>MoleRunner " +
                "commands</i></strong></h3><hr></html>");
        startButton = new JButton("START");
        terminateButton = new JButton("TERMINATE");
        JLabel moleCommandsLabel = new JLabel("<html><h3><strong><i>Sequence Mole " +
                "commands</i></strong></h3><hr></html>");
        stepButton = new JButton("STEP");
        skipButton = new JButton("SKIP");
        resumeButton = new JButton("RESUME");
        pauseButton = new JButton("PAUSE");
        JLabel commandsResponsesLabel = new JLabel("<html><h3><strong><i>Command " +
                "responses</i></strong></h3><hr></html>");
        commandResponsesList= new JList<>(commandsResponsesListModel);
        commandResponsesList.setBorder(new LineBorder(Color.BLACK));

        JPanel eventsPanel = new JPanel();
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.PAGE_AXIS));
        JLabel eventsLabel = new JLabel("<html><h3><strong><i>Events</i></strong></h3><hr></html>");
        eventsList= new JList<>(eventsListModel);
        eventsList.setBorder(new LineBorder(Color.BLACK));

        JPanel statesPanel = new JPanel();
        statesPanel.setLayout(new BoxLayout(statesPanel, BoxLayout.PAGE_AXIS));
        JLabel statesLabel = new JLabel("<html><h3><strong><i>States</i></strong></h3><hr></html>");
        statesList= new JList<>(statesListModel);
        statesList.setBorder(new LineBorder(Color.BLACK));


        startButton.setEnabled(false);
        terminateButton.setEnabled(false);
        stepButton.setEnabled(false);
        skipButton.setEnabled(false);
        resumeButton.setEnabled(false);
        pauseButton.setEnabled(false);

        commandsPanel.add(instantiateButton);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(moleRunnerCommandsLabel);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(startButton);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(terminateButton);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(moleCommandsLabel);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(stepButton);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(skipButton);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(resumeButton);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(pauseButton);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(commandsResponsesLabel);
        commandsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        commandsPanel.add(commandResponsesList);

        eventsPanel.add(eventsLabel);
        eventsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        eventsPanel.add(eventsList);

        statesPanel.add(statesLabel);
        statesPanel.add(Box.createRigidArea(new Dimension(0,5)));
        statesPanel.add(statesList);

        horizontalPanel.add(commandsPanel);
        horizontalPanel.add(eventsPanel);
        horizontalPanel.add(statesPanel);

        instantiateButton.addActionListener(e -> {
            instantiateButton.setEnabled(false);
            service.instantiate(SequenceMissionExample.class.getName(), null)
                    .subscribe(new SimpleSubscriber<ClientMissionController>() {
                        @Override
                        public void consume(ClientMissionController controller) {
                            controller.getEventsStream().subscribe(new SimpleSubscriber<MissionEvent>() {

                                @Override
                                public void consume(MissionEvent event) {
                                    if (event instanceof MissionRunnerEvent || event instanceof MissionFinished ||
                                            event instanceof MissionExceptionEvent) {
                                        eventsListModel.addElement("<html><font color='green'>" + event + "</font>"+
                                                "</html>");
                                    } else if (event instanceof SequenceMissionEvent) {
                                        eventsListModel.addElement("<html><font color='blue'>" + event + "</font>"+
                                                "</html>");
                                    } else {
                                        eventsListModel.addElement(event.toString());
                                    }
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
                                    switch (state.getLevel()) {
                                        case MOLE_RUNNER:
                                            statesListModel.addElement("<html><font color='green'>" + state + "</font>"+
                                                    "</html>");
                                            break;
                                        case MOLE:
                                            statesListModel.addElement("<html><font color='blue'>" + state + "</font>"+
                                                    "</html>");
                                            break;
                                    }
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
                                            displayCommandResponse(new MissionControlCommand(MissionControlCommand.Command
                                                    .START), response);
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
                                            displayCommandResponse(new MissionControlCommand(MissionControlCommand.Command
                                                    .TERMINATE), response);
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
                                            displayCommandResponse(new SequenceCommand(SequenceCommand.Command
                                                    .STEP), response);
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
                                            displayCommandResponse(new SequenceCommand(SequenceCommand.Command
                                                    .SKIP), response);
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            throwable.printStackTrace();
                                        }

                                        @Override
                                        public void onComplete() {

                                        }
                                    }));

                            resumeButton.addActionListener(e1 -> controller.instruct(new SequenceCommand
                                    (SequenceCommand.Command.RESUME))
                                    .subscribe(new SimpleSubscriber<CommandResponse>() {
                                        @Override
                                        public void consume(CommandResponse response) {
                                            displayCommandResponse(new SequenceCommand(SequenceCommand.Command
                                                    .RESUME), response);
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            throwable.printStackTrace();
                                        }

                                        @Override
                                        public void onComplete() {

                                        }
                                    }));
                            pauseButton.addActionListener(e1 -> controller.instruct(new SequenceCommand
                                    (SequenceCommand.Command.PAUSE))
                                    .subscribe(new SimpleSubscriber<CommandResponse>() {
                                        @Override
                                        public void consume(CommandResponse response) {
                                            displayCommandResponse(new SequenceCommand(SequenceCommand.Command
                                                    .PAUSE), response);
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
                    });
        });



        frame.getContentPane().add(horizontalPanel);

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
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(false);
                for (MissionCommand command : state.getPossibleCommands()) {
                    if (command instanceof SequenceCommand && ((SequenceCommand) command).getCommand()
                            .equals(SequenceCommand.Command.STEP)) {
                        stepButton.setEnabled(true);
                    } else if (command instanceof SequenceCommand && ((SequenceCommand) command).getCommand()
                            .equals(SequenceCommand.Command.SKIP)) {
                        skipButton.setEnabled(true);
                    } else if (command instanceof SequenceCommand && ((SequenceCommand) command).getCommand()
                            .equals(SequenceCommand.Command.RESUME)) {
                        resumeButton.setEnabled(true);
                    } else if (command instanceof SequenceCommand && ((SequenceCommand) command).getCommand()
                            .equals(SequenceCommand.Command.PAUSE)) {
                        pauseButton.setEnabled(true);
                    }
                }
                break;
        }
    }

    private void displayCommandResponse(MissionCommand command, CommandResponse commandResponse) {
        commandResponse.execute((throwable) -> commandsResponsesListModel.addElement("<html><font color='red'>command "
                        + command + " rejected: " + commandResponse + "</font></html>"),(ack) ->
                commandsResponsesListModel.addElement("<html><font color='green'>command " + command + " accepted: " +
                        commandResponse + "</font></html>"));
    }
}
