package io.molr.commons.domain;

public enum BlockAttribute {
	BREAK,
	IGNORE,
	ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS,
	/*
	 * If execution strategy is PROCEED_ON_ERROR blocks marked with attribute will still cause strand execution being abort
	 */
	FORCE_ABORT_ON_ERROR
}
