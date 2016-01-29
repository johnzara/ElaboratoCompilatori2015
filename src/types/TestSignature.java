package types;

import javaBytecodeGenerator.JavaClassGenerator;
import javaBytecodeGenerator.TestClassGenerator;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.MethodGen;

import translation.Block;
import absyn.TestDeclaration;
import bytecode.CONSTRUCTORCALL;
import bytecode.LOAD;


public class TestSignature extends CodeSignature {
    
	/**
     * Costruisco una segnatura per un oggetto Test
     *
     * @param clazz la classe dove il codice è definito
     * @param name il nome di questo codice
     * @param abstractSyntax la sintassi astratta della dichiarazione del codice
     */
	
    public TestSignature(ClassType clazz, String name, TestDeclaration abstractSyntax) {
    	super(clazz,BooleanType.INSTANCE , TypeList.EMPTY, name, abstractSyntax);
    }
    
    
    /**
     * Facciamo l'override del metodo equals
     * per consentire il test equals solo tra classi
     * di tipo TestSignature
     */
    /*@Override
    public boolean equals(Object other) {
    		if(getClass() == other.getClass()){
    			TestSignature otherT = (TestSignature) other;
    			return getName() == otherT.getName();
    		}
    		else
    			return false;
    }*/

    /**
     * Override della classe to string (ometto i parametri)
     */
    @Override
    public String toString() {
    	return getDefiningClass() + "."
   			+ getName() + "=test";
    }
    
    /**
     * Aggiungo un prefisso al kitten bytecode generato per questa fixture.
     * Questo permette alle istanze dei costruttori di aggiungere una chiamata al
     * costruttore della superclasse.
     *
     * @param il codice già compilato per questa fixture
     * @return {@code code} con un prefisso
     */

    protected Block addPrefixToCode(Block code){
    	return code;
    }

    public void createTest(TestClassGenerator classGen) {
		MethodGen methodGen;
		methodGen = new MethodGen
		(Constants.ACC_PRIVATE | Constants.ACC_STATIC, // private and static
		//org.apache.bcel.generic.Type.getType(runTime.String), // return type
		new org.apache.bcel.generic.ObjectType(runTime.String.class.getName()),
		new org.apache.bcel.generic.Type[]{getDefiningClass().toBCEL()},
		null, // parameters names: we do not care
		getName(), // method's name
		classGen.getClassName(), // defining class
		classGen.generateJavaBytecode(getCode()), // bytecode of the method
		classGen.getConstantPool()
		); // constant pool
    
		// we must always call these methods before the getMethod()
		// method below. They set the number of local variables and stack
		// elements used by the code of the method
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// we add a method to the class that we are generating
		classGen.addMethod(methodGen.getMethod());
    }
    
	public INVOKESTATIC createINVOKESTATIC(JavaClassGenerator classGen) {
		return (INVOKESTATIC) createInvokeInstruction(classGen, Constants.INVOKESTATIC);
	}
    
}