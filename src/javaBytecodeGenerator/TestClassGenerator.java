package javaBytecodeGenerator;

import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import translation.Block;
import types.ClassMemberSignature;
import types.ClassType;
import types.FixtureSignature;
import types.TestSignature;

/**
 * A Java bytecode generator. It transforms the Kitten intermediate language
 * into Java bytecode that can be dumped to Java class files and run.
 * It uses the BCEL library to represent Java classes and dump them on the file-system.
 *
 */


@SuppressWarnings("serial")
public class TestClassGenerator extends JavaClassGenerator {

	ClassType clazz;
	
	public TestClassGenerator(ClassType clazz, Set<ClassMemberSignature> sigs) {
		super(clazz.getName() + "Test", // nome della classe
				// settiamo java.lang.Object come superclasse della classe Kitten Object 
				clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "java.lang.Object",
				clazz.getName() + ".kit" // il file sorgente
				);

		this.clazz = clazz;
		// aggiunta delle fixture
		for (FixtureSignature fix: clazz.getFixtures())
			fix.createFixture(this);
		// aggiunta dei test
		for (TestSignature test: clazz.getTests())
			test.createTest(this);
		
		this.createMain();
	}
	
	public void createMain() {
		MethodGen methodGen;
			methodGen = new MethodGen
				(Constants.ACC_PUBLIC | Constants.ACC_STATIC, // public and static
				org.apache.bcel.generic.Type.VOID, // return type
				new org.apache.bcel.generic.Type[] // parameters
					{ new org.apache.bcel.generic.ArrayType("java.lang.String", 1) },
				null, // parameters names: we do not care
				"main", // method's name
				this.getClassName(), // defining class
				new InstructionList(),
				this.getConstantPool()); // constant pool
		
		// generazione delle variabili locali per tempo di esecuzione, tempo del test e contatore dei test totali
		LocalVariableGen time = methodGen.addLocalVariable("time", Type.INT, null, null);
		LocalVariableGen testTime = methodGen.addLocalVariable("testTime", Type.INT, null, null);
		LocalVariableGen count = methodGen.addLocalVariable("count", Type.INT, null, null);
		
		// istruzioni BCEL. InstructionList contiene una lista di oggetti Instruction
		InstructionList iList = new InstructionList();
		
		// inizializzazione delle variabili locali a 0

		iList.append(InstructionFactory.ICONST_0);
		iList.append(new ISTORE(count.getIndex()));
		
		iList.append(InstructionFactory.ICONST_0);
		iList.append(new ISTORE(time.getIndex()));
		
		iList.append(InstructionFactory.ICONST_0);
		iList.append(new ISTORE(testTime.getIndex()));	

		//creazione getstatic per la stampa a console
		iList.append(getFactory().createGetStatic("java/lang/System", "out", 
				Type.getType(java.io.PrintStream.class)));
		
		// costruisco stringbuffer
		iList.append(getFactory().createNew(Type.STRINGBUFFER));
		//tramite DUP viene duplicata la cima dello stack
		iList.append(InstructionFactory.DUP);
		//constant pool contiene la tabella delle costanti della classe corrente
		//LDC -> push item from constant pool
		iList.append(new LDC(getConstantPool().
				addString("\n \n \nTest execution for class " + this.clazz.getName() + "\n")));
		//creo una INVOKESPECIAL tramite createInvoke(nome_classe, nome_metodo chiamato, tipo_ritorno, argomenti_metodo, tipo_invoke)
		iList.append(getFactory().createInvoke("java.lang.StringBuffer", "<init>",
	                Type.VOID, new Type[]{Type.STRING},
	                Constants.INVOKESPECIAL));

		//instructionHandle utile per accedere alle istruzioni contenute in instructionList
		InstructionHandle inizioTempoTot = iList.getEnd();
		//creo una instructionList per ogni test
		for (TestSignature test: clazz.getTests()) {
			InstructionList ilTest = new InstructionList();
			
			// concateno il nome del test con stringbuffer
			ilTest.append(new LDC(getConstantPool().addString("\t- " + test.getName() + ": ")));
			ilTest.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
	                Type.STRINGBUFFER, new Type[]{Type.STRING},
	                Constants.INVOKEVIRTUAL));

			// creo il test che ritornerà una stringa Kitten
			ilTest.append(finalTest(test, clazz.getFixtures()));
			// invoco la toString() che consuma la stringa Kitten e ritorna una stringa Java
			ilTest.append(getFactory().createInvoke("runTime/String", "toString",
                Type.STRING, Type.NO_ARGS,
	                Constants.INVOKEVIRTUAL));
			//duplico la stringa
			ilTest.append(InstructionFactory.DUP);
			//aggiungo la stringa "passed" alla constant pool e la carico sullo stack con LDC
			//confronto il risultato dell'esecuzione del test ritornato dall'assert con la stringa "passed"
			//se già presente ne ritorno solo il riferimento
			ilTest.append(new LDC(getConstantPool().addString("passed")));
			//chiamo equals su String, che ritorna 1 se gli argomenti sono uguali, altrimenti 0
			//equals viene eseguita sui primi due valori dello stack, contenenti il risultato del test e la stringa "passed"
			//stack:	passed
			//				passed
			//equals = 1
			ilTest.append(getFactory().createInvoke("java/lang/String", "equals",
	                Type.BOOLEAN, new Type[]{Type.OBJECT},
	                Constants.INVOKEVIRTUAL));
		
			//carico il contatore sullo stack
			ilTest.append(new ILOAD(count.getIndex()));
			//sommo i valori
			ilTest.append(InstructionFactory.IADD);
			//salvo il valore nel contatore
			ilTest.append(new ISTORE(count.getIndex()));
						
			// concateno il test del risultato con stringbuffer
			// stringa risultante
			ilTest.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
	                Type.STRINGBUFFER, new Type[]{Type.STRING},
	                Constants.INVOKEVIRTUAL));
			
			finalTime(ilTest, testTime.getIndex());
			iList.append(ilTest);
		}
		
		// stampa l'ultima riga "n tests passed, m failed [xyz ms]"
		
		// # test passati
		iList.append(new ILOAD(count.getIndex()));
		iList.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
                Type.STRINGBUFFER, new Type[]{Type.INT},
                Constants.INVOKEVIRTUAL));

		iList.append(new LDC(getConstantPool().addString(" test(s) passed ")));
		iList.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
                Type.STRINGBUFFER, new Type[]{Type.STRING},
                Constants.INVOKEVIRTUAL));
				
		
		// # test falliti		
		iList.append(new LDC(getConstantPool().addInteger(clazz.getTests().size())));
		iList.append(new ILOAD(count.getIndex()));
		iList.append(InstructionFactory.ISUB);
		iList.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
                Type.STRINGBUFFER, new Type[]{Type.INT},
                Constants.INVOKEVIRTUAL));
		
				iList.append(new LDC(getConstantPool().addString(" failed")));
		iList.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
                Type.STRINGBUFFER, new Type[]{Type.STRING},
                Constants.INVOKEVIRTUAL));
		//calcolo tempo finale
		finalTime(iList, inizioTempoTot, iList.getEnd(), time.getIndex());
	
        iList.append(getFactory().createInvoke("java.lang.StringBuffer", "toString",
                Type.STRING, Type.NO_ARGS,
                Constants.INVOKEVIRTUAL));
        
		iList.append(getFactory().createInvoke(
				"java/io/PrintStream", 
				"print", 
				Type.VOID, 
				new org.apache.bcel.generic.Type[]{org.apache.bcel.generic.Type.STRING},
				org.apache.bcel.Constants.INVOKEVIRTUAL
		));
		iList.append(InstructionFactory.createReturn(Type.VOID));
		
			
		methodGen.setInstructionList(iList);
		// dobbiamo sempre chiamare questi metodi sotto prima di getMethod()
		// Settano il numero di variabili locali e gli elementi dello stack usato dal codice del metodo
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// aggiungo un metodo alla classe che viene generata
		this.addMethod(methodGen.getMethod());
	}

	private InvokeInstruction currentMillis() {
		return getFactory().createInvoke(
				"java/lang/System", 
				"currentTimeMillis", 
				org.apache.bcel.generic.Type.LONG, 
				new org.apache.bcel.generic.Type[]{},
				org.apache.bcel.Constants.INVOKESTATIC
		);
	}
	
	private void finalTime(InstructionList il, int index){
		finalTime(il, il.getStart(), il.getEnd(), index);
	}
	
	private void finalTime(InstructionList il, InstructionHandle start, InstructionHandle end, int index) {
		InstructionList prima = new InstructionList();
		prima.append(currentMillis());
		prima.append(InstructionConstants.L2I);
		prima.append(new ISTORE(index));
		
		InstructionList dopo = new InstructionList();
		// calcolo il tempo
		dopo.append(currentMillis());
		dopo.append(InstructionConstants.L2I);
		dopo.append(new ILOAD(index));;
		dopo.append(InstructionFactory.ISUB);
		dopo.append(new ISTORE(index));
		// creo la stringa [xyz ms] 
		dopo.append(new LDC(getConstantPool().addString(" [")));
		dopo.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
                Type.STRINGBUFFER, new Type[]{Type.STRING},
                Constants.INVOKEVIRTUAL));
		
		dopo.append(new ILOAD(index));
		dopo.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
                Type.STRINGBUFFER, new Type[]{Type.INT},
                Constants.INVOKEVIRTUAL));
		
		dopo.append(new LDC(getConstantPool().addString("ms]\n")));
		dopo.append(getFactory().createInvoke("java.lang.StringBuffer", "append",
                Type.STRINGBUFFER, new Type[]{Type.STRING},
                Constants.INVOKEVIRTUAL));

		il.append(start, prima);
		il.append(end, dopo);
	}
	
	/*
		Associo le fixture della classe al test passato come parametro
	*/
	private InstructionList finalTest(TestSignature test, Set<FixtureSignature> fixtures) {
		InstructionList il = new InstructionList();
		
		
		// Creo un nuovo oggetto
		il.append(getFactory().createNew(this.clazz.getName()));
		// stack:	obj
		
		// dup
		il.append(InstructionFactory.DUP);
		// stack:	obj
		//				obj

		// creo un oggetto O di tipo C
		il.append(getFactory().createInvoke(
				this.clazz.getName(), 
				"<init>", 
				org.apache.bcel.generic.Type.VOID, 
				new org.apache.bcel.generic.Type[]{},
				org.apache.bcel.Constants.INVOKESPECIAL
		));
		// stack:	obj
		
		//chiama ogni fixture di tipo C passando O come parametro
		for (FixtureSignature fixture: clazz.getFixtures()) {		
			// dup
			il.append(InstructionFactory.DUP);
			// stack:	obj
			//				obj
			il.append(getFactory().createInvoke(
					this.clazz.getName() + "Test", 
					fixture.getName(), 
					org.apache.bcel.generic.Type.VOID, 
					new org.apache.bcel.generic.Type[]{clazz.toBCEL()},
					org.apache.bcel.Constants.INVOKESTATIC
			));
		}
		
		// chiamo il test passando O come parametro
		il.append(getFactory().createInvoke(
				this.clazz.getName() + "Test", 
				test.getName(), 
				new org.apache.bcel.generic.ObjectType(runTime.String.class.getName()),
				new org.apache.bcel.generic.Type[]{clazz.toBCEL()},
				org.apache.bcel.Constants.INVOKESTATIC
		));
		
		return il;
	}
}