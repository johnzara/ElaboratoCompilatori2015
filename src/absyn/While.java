package absyn;

import java.io.FileWriter;

import semantical.TypeChecker;
import translation.Block;

/**
 * A node of abstract syntax representing a <tt>while</tt> command.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public class While extends Command {

	/**
	 * The guard or condition of the loop.
	 */

	private final Expression condition;

	/**
	 * The body of the loop.
	 */

	private final Command body;

	/**
	 * Constructs the abstract syntax of a {@code while} command.
	 *
	 * @param pos the position in the source file where it starts
	 *            the concrete syntax represented by this abstract syntax
	 * @param condition the guard or condition of the loop
	 * @param body the body of the loop
	 */

	public While(int pos, Expression condition, Command body) {
		super(pos);

		this.condition = condition;
		this.body = body;
	}

	/**
	 * Yields the abstract syntax of the guard or condition of the {@code while} command.
	 *
	 * @return the abstract syntax of the guard or condition of the {@code while} command
	 */

	public Expression getCondition() {
		return condition;
	}

	/**
	 * Yields the abstract syntax of the body of the {@code while} command.
	 *
	 * @return the abstract syntax of the body of the {@code while} command
	 */

	public Command getBody() {
		return body;
	}

	/**
	 * Adds abstract syntax class-specific information in the dot file
	 * representing the abstract syntax of the {@code while} command.
	 * This amounts to adding two arcs from the node for the {@code while}
	 * command to the abstract syntax for {@link #condition} and {@link #body}.
	 *
	 * @param where the file where the dot representation must be written
	 */

	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("condition", condition.toDot(where), where);
		linkToNode("body", body.toDot(where), where);
	}

	/**
	 * Performs the type-checking of the {@code while} command
	 * by using a given type-checker. It type-checks the condition and body
	 * of the {@code while} command. It checks that the condition is
	 * a Boolean expression. Returns the original type-checker
	 * passed as a parameter, so that local declarations in the body of
	 * the loop are not visible after.
	 *
	 * @param checker the type-checker to be used for type-checking
	 * @return {@code checker} itself
	 */

	@Override
	protected TypeChecker typeCheckAux(TypeChecker checker) {
		// la condizione del ciclo deve essere booleana
		condition.mustBeBoolean(checker);

		// viene eseguito il type-check di questo comando
		// Si noti che il type-checker risultante non viene utilizzato.
		body.typeCheck(checker);

		// dichiarazioni di variabili locali nel corpo del ciclo vengono perse dopo il ciclo stesso.
		return checker;
	}

	/**
	 * Checks that this {@code while} does not contain <i>dead-code</i>, that is,
	 * commands that can never be executed. It calls itself recursively on {@link #body}.
	 *
	 * @return false, since there is no guarantee that the loop will be entered at least once
	 */

	@Override
	public boolean checkForDeadcode() {
		body.checkForDeadcode();

		return false;
	}

	/**
	 * Translates this command into intermediate Kitten bytecode. Namely,
	 * it returns a code which evaluates the {@link #condition} and then continues with
	 * the compilation of {@link #body} or with the given {@code continuation}.
	 * After the {@link #body}, the {@link #condition} is checked again.
	 *
	 * @param continuation the continuation to be executed after this command
	 * @return the code executing this command and then the {@code continuation}
	 */

	@Override
	public Block translate(Block continuation) {
		/* The idea is to translate a while command into the code

	    condition -> (no) continuation
	    | (yes) ^
	    V       |
	    body ----
		 */

	  // creiamo un blocco vuoto che servirà a chiudere il ciclo
		Block pivot = new Block();

		// traduciamo la condizione del ciclo. Se essa è vera, eseguiamo la traduzione del corpo.
		// Altrimenti, eseguiamo la continuazione.
		Block result = condition.translateAsTest(body.translate(pivot), continuation);

		result.doNotMerge();

		// linkiamo il pivot al codice che testa la condizione, così da chiudere il ciclo
		pivot.linkTo(result);

		return result;
	}
}