package absyn;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import bytecode.ADD;
import bytecode.Bytecode;
import bytecode.BytecodeList;
import bytecode.CALL;
import bytecode.CONST;
import bytecode.GETFIELD;
import bytecode.NEWSTRING;
import bytecode.PUTFIELD;
import bytecode.RETURN;
import bytecode.VIRTUALCALL;
import semantical.TypeChecker;
import translation.Block;
import types.ClassMemberSignature;
import types.ClassType;
import types.CodeSignature;
import types.FixtureSignature;
import types.IntType;
import types.MethodSignature;
import types.TestSignature;
import types.TypeList;
import types.VoidType;



public class TestDeclaration extends CodeDeclaration {

  //nome del test

	private String name;


  /*
    Costruisce la sintassi astratta della dichiarazione del test.
    I parametri ricevuti sono pos, ossia il punto di partenza nel file del nodo di sintassi corrispondente;
    name è il nome del test; body è la sintassi astratta del corpo del test; next rappresenta la sintassi astratta
    del successivo membro di classe, se esiste
  */

	public TestDeclaration(int pos, String name, Command body, ClassMemberDeclaration next) {
		super(pos, null, body, next);
		this.name = name;
	}
	

  /*
    Metodo che ritorna il nome del test
  */

    public String getName() {
    	return name;
    }
    

  /*
    Metodo che ritorna la segnatura della dichiarazione del test. Se il type-checking non è ancora stato eseguito, 
    viene ritornato null.
  */

	@Override
	public TestSignature getSignature() {
		return (TestSignature) super.getSignature();
	}
    

  //aggiungo archi tra il nodo della sintassi astratta e i parametri formali + corpo della fixture
  //scrivo nel punto where del file dot
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("body", getBody().toDot(where), where);
	}
	
  
  //aggiungo la segnatura della dichiarazione di test dentro a clazz

	@Override 
	protected void addTo(ClassType clazz) {
		TestSignature cSig = new TestSignature (clazz, name, this);

		clazz.addTest(cSig);

    // viene registrata la segnatura di questo costruttore dentro la sintassi astratta
		setSignature(cSig);
	}

  /*
  Il seguente metodo effettua il type-check della dichiarazione del test, ossia costruisce
  un type-checker la cui unica variabile in scope è this, riferita alla classe di definizione della fixture,
  e l'unico valore di ritorno permesso è void. Successivamente esegue il type-check del corpo della fixture
  nel type-checker e verifica che non contenga codice morto. 
  Il parametro ricevuto è di tipo ClassType, ovvero il tipo semantico della classe in cui questo costruttore occorre.
  */

  //si implementano le operazioni spechifiche alla singola espressione
	@Override
	protected void typeCheckAux(ClassType clazz) {
		
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, clazz.getErrorMsg(), true);
		checker = checker.putVar("this", clazz);

    // esegue il type-check del corpo del test nel type-checker risultante
		getBody().typeCheck(checker);

    // verifica che non ci sia codice morto nel corpo del test
		getBody().checkForDeadcode();

    // I test non restituiscono nulla, perciò non occorre controllare che sia presente un return
    // alla fine di ogni percorso di esecuzione sintattico all'interno del corpo di un test
	}
	

    /*
    Traduce questo costruttore o metodo in codice intermedio Kitten.
    Questo significa tradurre il corpo con una continuazione contenente un bytecode di ritorno. 
    In questo modo, se un metodo non ritorna esplicitamente qualcosa, viene automaticamente aggiunto
    il return alla fine di esso. Il parametro ricevuto (done) è un set di segnature già tradotte.
    */

    public void translate(Set<ClassMemberSignature> done) {
    	if (done.add(getSignature())) {
    		
    		translateAux(getSignature().getDefiningClass(), done);

    		 /*Questo metodo traduce il corpo del costruttore o del metodo
            con un blocco contenente RETURN come continuazione. In questo modo,
            tutti i metodi che ritornano void o quelli senza return vengono terminati
            correttamente. Se il metodo è non-void questa precauzione è inutile dato che
            il metodo checkForDeadcode() garantisce che qualsiasi percorso 
            termina sicuramente con un return. */

    		Block post = new Block(new RETURN(ClassType.mk("String")));
        // viene creata la string che conterrà "assert passed"
    		post = new NEWSTRING("passed").followedBy(post);	

    		getSignature().setCode(getBody().translate(post));
        // traduce in kitten bytecode tutti i membri della classe a cui fa riferimento il blocco 
    		translateReferenced(getSignature().getCode(), done, new HashSet<Block>()); 
    	}
    }
}