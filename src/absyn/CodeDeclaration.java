package absyn;

import java.util.HashSet;
import java.util.Set;

import translation.Block;
import types.ClassMemberSignature;
import types.ClassType;
import types.CodeSignature;
import types.ConstructorSignature;
import types.FieldSignature;
import types.FixtureSignature;
import types.MethodSignature;
import types.TestSignature;
import types.VoidType;
import bytecode.Bytecode;
import bytecode.BytecodeList;
import bytecode.CALL;
import bytecode.GETFIELD;
import bytecode.PUTFIELD;
import bytecode.RETURN;

/**
 * A node of abstract syntax representing the declaration of a constructor
 * or method of a Kitten class.
 *
 * @author  <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public abstract class CodeDeclaration extends ClassMemberDeclaration {

    /**
     * The abstract syntax of the formal parameters of the constructor
     * or method. This might be {@code null}.
     */

    private final FormalParameters formals;

    /**
     * The abstract syntax of the body of the constructor or method.
     */

    private final Command body;

    /**
     * The signature of this constructor or method. This is {@code null} if this
     * constructor or method has not been type-checked yet.
     */

    private CodeSignature sig;

    /**
     * Constructs the abstract syntax of a constructor or method declaration.
     *
     * @param pos the starting position in the source file of
     *            the concrete syntax represented by this abstract syntax
     * @param formals the abstract syntax of the formal parameters of the constructor or method
     * @param body the abstract syntax of the body of the constructor or method
     * @param next the abstract syntax of the declaration of the next class member, if any
     */

    protected CodeDeclaration(int pos, FormalParameters formals, Command body, ClassMemberDeclaration next) {
    	super(pos,next);

    	this.formals = formals;
    	this.body = body;
    }

    /**
     * Yields the abstract syntax of the formal parameters of the constructor or method.
     *
     * @return the abstract syntax of the formal parameters of the constructor or method
     */

    public FormalParameters getFormals() {
    	return formals;
    }

    /**
     * Yields the abstract syntax of the body of the constructor or method.
     *
     * @return the abstract syntax of the body of the constructor or method
     */

    public Command getBody() {
    	return body;
    }

    /**
     * Specifies the code signature of this declaration.
     *
     * @param sig the code signature of this declaration
     */

    protected void setSignature(CodeSignature sig) {
    	this.sig = sig;
    }

    /**
     * Yields the signature of this method or constructor declaration.
     *
     * @return the signature of this method or constructor declaration.
     *         Yields {@code null} if type-checking has not been performed yet
     */

    @Override
    public CodeSignature getSignature() {
    	return sig;
    }

    /**
     * Translates this constructor or method into intermediate Kitten code.
     * This amounts to translating its body with a continuation containing
     * a {@code return} bytecode. This way, if a method does not have an
     * explicit {@code return} statement, it is automatically put at its end.
     *
     * @param done the set of code signatures that have been already translated
     */
    public void translate(Set<ClassMemberSignature> done) {
    	if (done.add(sig)) {
    		translateAux(sig.getDefiningClass(), done);
            /*
            Questo metodo traduce il corpo del costruttore o del metodo
            con un blocco contenente RETURN come continuazione. In questo modo,
            tutti i metodi che ritornano void o quelli senza return vengono terminati
            correttamente. Se il metodo è non-void questa precauzione è inutile dato che
            il metodo checkForDeadcode() garantisce che qualsiasi percorso 
            termina sicuramente con un return. 
            */
    		sig.setCode(getBody().translate(new Block(new RETURN(VoidType.INSTANCE))));

            // vengono tradotti tutti i metodi e i costruttori riferiti al codice generato.
    		translateReferenced(sig.getCode(), done, new HashSet<Block>());
    	}
    }

    /**
     * Auxiliary method that translates into Kitten bytecode all class members that are
     * referenced from the given block and the blocks reachable from it.
     *
     * @param block the block
     * @param done the class member signatures already translated
     * @param blocksDone the blocks that have been already processed
     */
 
    protected void translateReferenced(Block block, Set<ClassMemberSignature> done, Set<Block> blocksDone) {
        // se il blocco è già stato processato, si ritorna immediatamente
    	if (!blocksDone.add(block))
    		return;

    	for (BytecodeList cursor = block.getBytecode(); cursor != null; cursor = cursor.getTail()) {
    		Bytecode h = cursor.getHead();

    		if (h instanceof GETFIELD) {
    			done.add(((GETFIELD) h).getField());
    			translateAux(((GETFIELD) h).getField().getDefiningClass(), done);
    			
    		}
    		else if (h instanceof PUTFIELD) {
    			done.add(((PUTFIELD) h).getField());
    			translateAux(((PUTFIELD) h).getField().getDefiningClass(), done);
    		}
    		else if (h instanceof CALL) {
    			for (CodeSignature callee: ((CALL) h).getDynamicTargets()) {
    				callee.getAbstractSyntax().translate(done);
    				translateAux(callee.getDefiningClass(), done);
    			}
    		}
    	}

    	// si continua ricorsivamente con i blocchi successivi
    	for (Block follow: block.getFollows())
    		translateReferenced(follow, done, blocksDone);
    }
    
    /**
     * Auxiliary method that translates into Kitten bytecode the fixtures, the test
     * and the constructors of the given class.
     *
     * @param clazz the type of the class
     * @param done the class member signatures already translated
     */

    protected void translateAux(ClassType clazz, Set<ClassMemberSignature> done) {
    	for(FixtureSignature cms : clazz.getFixtures()){
    		cms.getAbstractSyntax().translate(done);
    	}

    	for(TestSignature cms : clazz.getTests()){
    		cms.getAbstractSyntax().translate(done);
    	}
    	
    	for(ConstructorSignature cms : clazz.getConstructors()){
    		cms.getAbstractSyntax().translate(done);
    	}
    }
}