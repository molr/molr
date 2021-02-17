package io.molr.mole.core.tree;

import java.text.MessageFormat;

import io.molr.commons.domain.StrandCommand;

public class QueuedCommand {
	
	private final StrandCommand strandCommand;
	private final long commandId;

	public QueuedCommand(StrandCommand strandCommand, long commandId) {
		super();
		this.strandCommand = strandCommand;
		this.commandId = commandId;
	}
	public StrandCommand getStrandCommand() {
		return strandCommand;
	}
	public long getCommandId() {
		return commandId;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("'{'command:{0}, id:{1}'}'", strandCommand.toString(), commandId);
	}
	

}
