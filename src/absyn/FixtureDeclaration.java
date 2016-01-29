package absyn;

import java.io.FileWriter;

import semantical.TypeChecker;
import types.ClassType;
import types.FixtureSignature;
import types.VoidType;


public class FixtureDeclaration extends CodeDeclaration {
	/*
	Costruisce la sintassi astratta della dichiarazione di una fixture.
	pos rappresenta la posizione di partenza nel file sorgente della sintassi reale rappresentata
	dalla sintassi astratta
	body rappresenta la sintassi astratta del corpo della fixture
	next è la sintassi astratta della dichiarazione del successivo membro della classe (se esiste)
	*/
	private static int counter;
	private String name;
	
	public FixtureDeclaration(int pos, Command body, ClassMemberDeclaration next) {
		super(pos,null, body, next);
		name = "fixture".concat(String.valueOf(++counter));
	}
	
	/*
	Aggiunge gli archi tra la sintassi astratta corrente 
	e quella che rappresenta i parametri formali e il corpo della fixture. Riceve come parametro il file 
	dove la rappresentazione dot deve essere scritta.
	*/
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("body", getBody().toDot(where), where);
	}
	

	//aggiunge la segnatura della fixture alla classe corrente (clazz)

	@Override 
	protected void addTo(ClassType clazz) {
		FixtureSignature cSig = new FixtureSignature (clazz, name, this);
		
		clazz.addFixture(cSig);

		// viene salvata la segnatura di questo costruttore dentro la sintassi astratta
		setSignature(cSig);
	}

	//eseguo il type-check della fixture creando un type-checker la cui unica variabile in scope è this della classe
	//contenente la fixture ed è permesso utilizzare solo istruzioni che ritornano void.
	//Controllo poi che il corpo della fixture in quel type-checker non abbia codice morto.
	//vengono implementate le operazioni specifiche alla singola espressione
	@Override
	protected void typeCheckAux(ClassType clazz) {
		
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, clazz.getErrorMsg());
		checker = checker.putVar("this", clazz);


		// si effettua il type-check del corpo della fixture nel type-checker risultante
		getBody().typeCheck(checker);


		// ci si assicura che non sia presente del codice morto nel corpo della fixture
		getBody().checkForDeadcode();

		/* Le fixture non restituiscono nulla, perciò non occorre controllare che sia presente un return
		alla fine di ogni percorso di esecuzione sintattico all'interno del corpo di una fixture
		*/
	}
}