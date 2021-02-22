package io.molr.mole.core.tree.executor;

import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.rules.Timeout;

public abstract class TimeoutEnabledTest {

	public static long DEFAULT_TIMEOUT = 5000;
	
	@Rule
	public Timeout globalTimeout= new Timeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
	
}
