package bytecode;

import translation.Block;

/**
 * A sequential bytecode of the intermediate Kitten language, that is,
 * a bytecode that never gives rise to a branching when executed and that
 * has always a successor bytecode.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public abstract class SequentialBytecode extends NonBranchingBytecode {

	/**
	 * Builds a sequential bytecode.
	 */

	protected SequentialBytecode() {}

	/**
	 * Builds a block of code that contains this bytecode and is
	 * followed by the given {@code continuation}.
	 *
	 * @param continuation what follows this bytecode
	 * @return a block of code that contains this bytecode and then {@code continuation}
	 */

	//occorre specificare un blocco di codice

	public Block followedBy(Block continuation) {
		// ritorna un blocco con questo bytecode anteposto
		return continuation.prefixedBy(this);
	}
}