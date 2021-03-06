package bytecode;

/**
 * A bytecode of the intermediate Kitten language. It is a more typed,
 * simplified version of the Java bytecode.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public abstract class Bytecode {

    /**
     * Constructs a bytecode.
     */

    protected Bytecode() {}

    @Override
    public String toString() {
      // nome della classe. Le sottoclassi devono ridefinirlo
    	return getClass().getSimpleName().toLowerCase();
    }
}