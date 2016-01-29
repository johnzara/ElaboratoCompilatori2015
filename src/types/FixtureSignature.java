package types;

import javaBytecodeGenerator.JavaClassGenerator;
import javaBytecodeGenerator.TestClassGenerator;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.MethodGen;

import translation.Block;
import absyn.FixtureDeclaration;
import bytecode.CONSTRUCTORCALL;
import bytecode.LOAD;


public class FixtureSignature extends CodeSignature {

    /**
     * Costruisco una segnatura per un oggetto Fixture
     *
     * @param clazz la classe dove il codice è definito
     * @param name il nome di questo codice
     * @param abstractSyntax la sintassi astratta della dichiarazione del codice
     */

    public FixtureSignature(ClassType clazz, String name, FixtureDeclaration abstractSyntax) {
    	super(clazz, VoidType.INSTANCE, TypeList.EMPTY, name, abstractSyntax);
    }


  //Aggiungo un prefisso al bytecode kitten generato per questa fixture
  //come parametro il metodo riceve il codice già compilato per questo metodo e lo ritorna.

	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}
	
    @Override
    public String toString() {
    	return getDefiningClass() + "." + getName();
    }

    public void createFixture(TestClassGenerator classGen) {
		MethodGen methodGen;
		//Creiamo un generatore di metodi
		methodGen = new MethodGen
		(Constants.ACC_PRIVATE | Constants.ACC_STATIC, // private e static
		org.apache.bcel.generic.Type.VOID, // Tipo di ritorno
		new org.apache.bcel.generic.Type[]{getDefiningClass().toBCEL()},
		null, // nomi dei parametri, non necessari
		getName(), // nome del metodo
		classGen.getClassName(), // nome della classe
		classGen.generateJavaBytecode(getCode()), // bytecode del metodo
		classGen.getConstantPool()
		); // constant pool
		
		/*
		 * Questi metodi devono essere chiamati prima del getMethod().
		 * Servono per settare i numeri delle variabili locali e degli elementi dello stack
		 * usati dal codice del metodo
		 */
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		//Aggiungiamo il metodo alla classe che stiamo generando.
		classGen.addMethod(methodGen.getMethod());
    }
    
	public INVOKESTATIC createINVOKESTATIC(JavaClassGenerator classGen) {
		return (INVOKESTATIC) createInvokeInstruction(classGen, Constants.INVOKESTATIC);
	}
}