package absyn;

import java.io.FileWriter;

import bytecode.CONST;
import bytecode.NEWSTRING;
import bytecode.RETURN;
import bytecode.VIRTUALCALL;
import semantical.TypeChecker;
import translation.Block;
import types.BooleanType;
import types.ClassType;
import types.CodeSignature;
import types.IntType;
import types.ReferenceType;
import types.TypeList;
import types.VoidType;


public class Assert extends Command {

	/**
	 * Sintassi astratta dell'espressione su cui fare l'assert
	 */

	private Expression condition;

	//Costruisco la sintassi astratta di un comando assert
	//pos indica la posizione nel file dove inizia il comando assert
	//condition è la sintassi astratta del comando

	public Assert(int pos, Expression condition) {
		super(pos);

		this.condition = condition;
	}


	//aggiungo le informazioni della sintassi astratta nel file dot che rappresenta la sintassi astratta del comando
	//facendo un arco dal nodo assert alla sintassi astratta di condition

	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
			linkToNode("asserted", condition.toDot(where), where);
	}


	//questo metodo effettua il type-checking del nodo assert
	//utilizzando il type-checker passato come parametro. Verifica essenzialmente che la condizione sia un'espressione 
	//booleana. Ritorna lo stesso type-checker ricevuto come parametro
	@Override
	protected TypeChecker typeCheckAux(TypeChecker checker) {
		// la condizione dell'assert deve essere un booleano
		condition.mustBeBoolean(checker);
		if(!checker.isAssertAllowed())
			error("assert not allowed outside of a test method");

			return checker;
	}

	//Non può esistere dead-code all'interno dell'assert

	@Override
	public boolean checkForDeadcode() {
		return false;
	}

	/*
		Traduciamo il comando di assert in codice intermedio Kitten.
		In particolare, questo metodo ritorna un blocco di codice che
		valuta la condizione condition e che poi prosegue con la compilazione
		del ramo then oppure else. Successivamente il metodo prosegue con continuation.
	*/
	@Override
	public Block translate(Block continuation) {
		String out = makeFailureMessage();
		//crea il blocco failed
		Block failed = new Block(new RETURN(ClassType.mk("String")));
		//con VIRTUALCALL viene chiamato il metodo output risalendo ricorsivamente le superclassi.
		//Il valore di ritorno del metodo viene salvato sullo stack.
		failed = new VIRTUALCALL(ClassType.mkFromFileName("String.kit"),
				ClassType.mkFromFileName("String.kit").methodLookup("output", TypeList.EMPTY)).followedBy(failed);
		failed = new NEWSTRING("assert " + out + "\n").followedBy(failed);
		failed = new NEWSTRING(out).followedBy(failed);
		//ora la condizione viene tradotta assumendo che sia sempre boolean (e lo è sicuramente dato il precedente controllo)
		//il metodo translateAsTest riceve come parametri continuation oppure il blocco failed precedentemente costruito.
		//se condition è vera si va in continuation altrimenti in failed
		return condition.translateAsTest(continuation, failed);

	}

	private String makeFailureMessage() {
		//String filename = getTypeChecker().getFileName();
		String pos = getTypeChecker().calcPos(getPos());
		return "failed at " + pos; 
		//return "\t\tAssert fallita @" + filename + ":" + pos + "\n";
	}
	
}