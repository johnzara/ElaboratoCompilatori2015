package tables;

/**
 * A table mapping symbols to objects.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

/*
    Table<> viene utilizzata per rappresentare gli ambienti
    Table<E> contiene i metodi astratti get(key) e put(key, value) che vengono implementate in NonEmptyTable e Emptytable
    rispettivamente. Un ambiente altro non è che una funzione che associa gli ID ai valori denotabili (posizioni di memoria,
    procedure, funzioni)
*/

public abstract class Table<E> {

    /**
     * An empty table.
     */

    @SuppressWarnings("rawtypes")
	private final static Table<?> EMPTY = new EmptyTable();

    /**
     * Yields the empty table.
     *
     * @param <T> the type of the elements that will be added to the empty table
     * @return the empty table
     */

    @SuppressWarnings("unchecked")
	public static <T> Table<T> empty() {
    	return (Table<T>) EMPTY;
    }

    /**
     * Returns the object bound to a given symbol, if any.
     *
     * @param key the symbol to look up for in the table
     * @return the object bound to {@code key}.
     *         Yields {@code null} if no object is bound to {@code key}
     */

    // Metodo astratto da implementare nelle sottoclassi che permette di ricavare il valore di tipo E legato a key

    public abstract E get(String key);

    /**
     * Builds a new table, identical to this, but where a given symbol is
     * bound to a given value. This table is not modified.
     *
     * @param key the symbol to be bound to the given value
     * @param value to value to be bound to the symbol
     * @return a symbol table identical to this except for {@code key}, that
     *         is bound to {@code value}
     */
    // Metodo astratto da implementare nelle sottoclassi che permette di legare key al valore E
    public abstract Table<E> put(String key, E value);
}