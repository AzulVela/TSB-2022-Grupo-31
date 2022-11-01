package clases;

import java.io.Serializable;
import java.util.*;

/**
 * La clase TSBHashtable está diseñada para contener diferentes objetos y catalogarlos utilizando el método
 * de dispersion Hashing con el corrector de colisiones de Direccionamiento Abierto (DA) el mismo contiene
 * las clases, métodos, atributos para su correcto funcionamiento. El proyecto está creado para
 * satisfacer los requerimientos propuestos desde la Catedra de TSB año 2021 como Trabajo Práctico Integrador.
 * @author noIng-Juan Cruz Villarruel, noIng-Genaro Bergesio, noIng-Juan Cruz Bosetti
 * @version 2.0 remastered Edition
 * @param <K> Es un valor que funcionara como "Key" y sera la forma de Indexar nuestro vector
 * @param <V> Es el valor que será guardada en la casilla y contendrá una "codificación" del valor que tiene
 *           el objeto.
 */
public class TSBHashTableDA<K, V> implements Map<K, V>, Cloneable, Serializable {

    private final static int MAX_SIZE = Integer.MAX_VALUE;

    /**
     * Representa una casilla abierta que está libre para usar.
     */
    public static final int OPEN = 0;
    /**
     * Representa una casilla con un valor adentro.
     */
    public static final int CLOSED = 1;
    /**
     * Representa una casilla que tenía algo, pero ahora se marca como "Muerta" y se puede reutilizar.
     */
    public static final int TOMB = 2;

    /**
     * El atributo principal de la clase contenedora de todos los datos importantes para el desarrollo
     * de la misma.
     */
    private Object[] table;

    /**
     * Esta es la capacidad inicial de la tabla que DEBE SER un número primo para que el método DA funcione.
     */
    private int initial_capacity;

    /**
     * Este atributo contabiliza la cantidad de elementos que posee nuestra tabla.
     */
    private int count;

    /**
     * Respetando el método de DA debemos tener constancia de que el factor de carga de datos en la tabla
     * no supere el 50%, ya que sino la búsqueda cuadrática tendera a valores ineficientes.
     */
    private float load_factor;

    /**
     * Es el tipo de vista que usa de referencia las Key
     */
    private transient Set<K> keySet = null;

    /**
     * Es el tipo de vista basado en una colección de Entry
     */
    private transient Set<java.util.Map.Entry<K, V>> entrySet = null;

    /**
     * Es la vista basada en la colección de valores que posee la misma.
     */
    private transient Collection<V> valores = null;

    /**
     * Es un atributo para llevar constancia a la hora de iterar.
     */
    protected transient int modCount;

    // ------------------- Constructores --------------------- //

    /**
     * Constructor predeterminado que establece manualmente los valores de capacidad(initial_capacity) y factor de carga(load factor)
     */
    public TSBHashTableDA() {
        this(11, 0.5F);
    }

    /**
     * Constructores con una sobrecarga donde uno carga la capacidad inicial
     *
     * @param initial_capacity es el largo que poseería nuestro vector al inicializarse
     */
    public TSBHashTableDA(int initial_capacity) {
        this(initial_capacity, 0.5F);
    }

    /**
     * Es el constructor con dos sobrecargas que además válida los valores que se asignan.
     *
     * @param initial_capacity capacidad inicial del vector que debe ser un número primo.
     * @param load_factor      factor de carga del vector que debe ser menor al 50%
     */
    public TSBHashTableDA(int initial_capacity, float load_factor) {

        //Comprueba dichos valores y si no los satisface establece los predeterminados.
        if (load_factor <= 0 || load_factor > 0.5) {
            load_factor = 0.5f;
        }
        //Comprueba que no tenga una longitud de 0 o negativa
        if (initial_capacity <= 0) {
            initial_capacity = 11;
        } else {
            //Si la capacidad no es prima lo convierte al siguiente primo
            if (!isPrime(initial_capacity)) {
                initial_capacity = nextPrime(initial_capacity);
            }
        }
        //Inicializa el vector de objetos
        this.table = new Object[initial_capacity];

        //Inicializa la tabla con entradas vaciás
        for (int i = 0; i < table.length; i++) {
            table[i] = new Entry<K, V>(null, null);
            //Esto llama al constructor de OPEN de Entry
        }
        //Inicializa todos los atributos
        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;
    }

    /**
     * Es una clase interna que representa las entradas (Entry) que va a tener nuestro vector,
     * la misma entrada posee un Key y un valor
     *
     * @param <K> Representando la Key que se usa para identificar el elemento
     * @param <V> Representando el valor que va a tener el objeto cuando se le aplica hashing.
     */
    private class Entry<K, V> implements java.util.Map.Entry<K, V> {

        /**
         * Es la llave de acceso
         */
        private K key;
        /**
         * El valor que contiene
         */
        private V valor;
        /**
         * El estado en el cual se encuentra
         */
        private int state;

        /**
         * Es el constructor de entradas vacías (OPEN)
         *
         * @param key   la llave de acceso a la entrada
         * @param valor el valor que contiene la entrada
         */
        public Entry(K key, V valor) {
            this(key, valor, 0);
        }

        /**
         * Es el constructor donde se asignan todos los valores
         *
         * @param key   la llave de acceso a la entrada
         * @param valor el valor que contiene la entrada
         * @param state un identificador propio del DA donde define el estado de la misma
         */
        public Entry(K key, V valor, int state) {
            this.key = key;
            this.valor = valor;
            this.state = state;
        }

        //----------  Getters ---------//
        public K getKey() {
            return key;
        }

        public V getValue() {
            return valor;
        }

        public int getState() {
            return state;
        }


        //--------- Setters -----------//
        public V setValue(V valor) {
            if (this.valor == null) {
                throw new IllegalArgumentException("El valor es nulo, pruebe nuevamente");
            } else {
                V old = this.valor;
                this.valor = valor;
                return old;
            }
        }

        /**
         * Fija el estado actual de la entrada y válida que sea un número dentro de los parámetros
         *
         * @param n El estado que se busca fijar
         */
        public void setState(int n) {
            if (n > 2 || n < 0) {
                throw new IllegalArgumentException("El valor esta fuera de rango");
            } else {
                this.state = n;
            }
        }

        /**
         * Este genera el valor que vamos a utilizar para almacenar
         *
         * @return un entero que surge de realizar esta operación a la llave y al valor
         */
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            return 61 * hash + Objects.hashCode(this.valor);
        }

        /**
         * Método booleano que nos permite chequear la homogeneidad de las entradas.
         *
         * @param obj El objeto el cual se busca comparar con el que ya existe
         * @return True el objeto es el mismo tipo de objeto (Entry) y False si no cumple dichos requerimientos.
         */
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                System.out.println("El objeto es Nulo");
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                TSBHashTableDA.Entry other = (TSBHashTableDA.Entry) obj;
                if (!Objects.equals(this.key, other.key)) {
                    return false;
                } else {
                    return Objects.equals(this.valor, other.valor);
                }
            }
        }

        /**
         * Método para devolver el contenido en forma de String
         *
         * @return un String con todos los datos que posee la entrada.
         */
        public String toString() {

            return "Entry{key=" + this.key + ", valor=" + this.valor + ", state=" + this.state + "}";
        }
    }

    // --------------- Vistas ----------------//

    /**
     * Es una clase interna que representa la vista de llaves (KeySet)
     */
    private class KeySet extends AbstractSet<K> {
        //Inicializa el iterador
        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        //Retoma la cantidad de objetos contados
        @Override
        public int size() {
            return TSBHashTableDA.this.count;
        }

        //Retoma un booleano dependiendo si contiene dicho objeto en la vista
        @Override
        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsKey(o);
        }

        //Remueve el objeto de la vista mientras el mismo no sea nulo
        @Override
        public boolean remove(Object o) {
            return (TSBHashTableDA.this.remove(o) != null);
        }

        //Borra el contenido de la vista
        @Override
        public void clear() {
            TSBHashTableDA.this.clear();
        }

        /**
         * Es la clase interna que se utiliza para recorrer la vista
         */
        private class KeySetIterator implements Iterator<K> {
            // índice de la lista anterior (si se requiere en remove())
            private int last_entry;
            // índice del elemento actual en el iterator (el que fue retornado
            // la última vez por next() y será eliminado por remove())
            private int current_entry;
            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;
            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterator comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public KeySetIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /**
             * Determina si hay al menos un elemento en la vista que no haya
             * sido retornado por next().
             *
             * @return un True si hay un valor siguiente (Para mostrar) y False si se terminó de recorrer
             */
            @Override
            public boolean hasNext() {
                //Inicializa una tabla de objetos
                Object[] table = TSBHashTableDA.this.table;
                //Chequea si esta vacía
                if (TSBHashTableDA.this.isEmpty()) {
                    return false;
                }
                //Pregunta si el punto actual está fuera de la tabla.
                if (current_entry >= table.length) {
                    return false;
                }
                //Establece la siguiente entrada como la actual + 1
                int next_entry = current_entry + 1;

                //Itera para encontrar todas las posiciones en donde la tabla contenga un objeto
                for (int i = next_entry; i < table.length; i++) {
                    Entry<K, V> entry = (Entry<K, V>) table[i];
                    if (entry.getState() == CLOSED) return true;
                }

                return false;
            }

            /**
             * Busca la siguiente Key disponible
             *
             * @return devuelve la siguiente Key o devuelve un error si no posee siguiente.
             */
            @Override
            public K next() {
                // control: fail-fast iterator...
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                int next_entry = current_entry;

                //Un ciclo for que recorre las entradas hasta que encuentre una cerrada
                next_entry++;
                while (((Entry<K, V>) TSBHashTableDA.this.table[next_entry]).getState() != CLOSED) next_entry++;

                //La última entrada se vuelve la actual
                last_entry = current_entry;
                current_entry = next_entry;
                next_ok = true;

                Entry<K, V> entry = (Entry<K, V>) TSBHashTableDA.this.table[current_entry];

                // Retorna el elemento encontrado
                return entry.getKey();
            }

            /**
             * Remueve el elemento actual de la tabla, dejando el iterator en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * solo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // Elimina el objeto que retornó next() la última vez
                Entry<K, V> garbage = (Entry<K, V>) TSBHashTableDA.this.table[current_entry];
                garbage.setState(2);
                garbage.setValue(null);

                if (last_entry != current_entry) {
                    current_entry = last_entry;
                }

                //Avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // Se sustrae uno de la cuenta de objetos
                TSBHashTableDA.this.count--;

                // El controlador de errores del iterador
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /**
     * Es una clase interna que representa la vista por entradas.
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        /**
         * Pregunta si contiene el objeto dentro de la vista
         *
         * @param o Objeto que se está buscando
         * @return True si lo encuentra y False si no está en la vista.
         */
        public boolean contains(Object o) throws ClassCastException, NullPointerException {
            if (o == null) {
                return false;
            }
            Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            int index = TSBHashTableDA.this.h(key);
            return search_for_entry(key, index) != null;
        }

        /**
         * Este método se encarga de remover un objeto de la vista
         *
         * @param o el objeto que se busca remover
         * @return un True si fue posible y False si se produjo un error
         */
        public boolean remove(Object o) throws ClassCastException, NullPointerException {
            if (o == null) {
                throw new NullPointerException("remove(): parámetro null");
            }
            Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            int index = TSBHashTableDA.this.h(key);
            if (search_for_entry(key, index) == entry) {
                entry.setState(TOMB);
                entry.setValue(null);
                TSBHashTableDA.this.count--;
                TSBHashTableDA.this.modCount++;
                return true;
            }
            return false;
        }

        /**
         * Devuelve el largo de la vista
         *
         * @return un numero entero que lo representa
         */
        public int size() {
            return TSBHashTableDA.this.count;
        }

        @Override
        public void clear() {
            TSBHashTableDA.this.clear();
        }

        /**
         * Es la clase interna que se utiliza para recorrer la vista EntrySet
         */
        private class EntrySetIterator implements Iterator<Map.Entry<K, V>> {
            // índice de la lista anterior (si se requiere en remove())...
            private int last_entry;
            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int current_entry;
            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /**
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public EntrySetIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /**
             * Determina si hay al menos un elemento en la vista que no haya
             * sido retornado por next().
             *
             * @return un True si hay un valor siguiente (Para mostrar) y False si se terminó de recorrer
             */
            @Override
            public boolean hasNext() {
                Object[] table = TSBHashTableDA.this.table;

                if (TSBHashTableDA.this.isEmpty()) {
                    return false;
                }
                if (current_entry >= table.length) {
                    return false;
                }

                int next_entry = current_entry + 1;
                for (int i = next_entry; i < table.length; i++) {
                    Entry<K, V> entry = (Entry<K, V>) table[i];
                    if (entry.getState() == CLOSED) return true;
                }
                return false;
            }

            /**
             * Busca el siguiente elemento disponible
             *
             * @return una entrada que se encuentra a continuación en la lista
             */
            @Override
            public Map.Entry<K, V> next() {
                // control: fail-fast iterator...
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                int next_entry = current_entry;

                next_entry++;
                while (((Entry<K, V>) TSBHashTableDA.this.table[next_entry]).getState() != CLOSED) next_entry++;

                last_entry = current_entry;
                current_entry = next_entry;

                next_ok = true;

                // Retoma la entrada que se encontró
                return (Entry<K, V>) TSBHashTableDA.this.table[current_entry];
            }


            /**
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * solo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()");
                }

                // Elimina el objeto que busco Next()
                Entry<K, V> garbage = (Entry<K, V>) TSBHashTableDA.this.table[current_entry];
                garbage.setState(2);
                garbage.setValue(null);

                if (last_entry != current_entry) {
                    current_entry = last_entry;
                }

                //Avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                //La tabla tiene un elemento menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /**
     * Es una vista que muestra lo que se encuentra almacenado en forma de una colección de valores.
     */
    private class ValueCollection extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueCollectionIterator();
        }

        public int size() {
            return TSBHashTableDA.this.count;
        }

        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsValue(o);
        }

        public void clear() {
            TSBHashTableDA.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V> {
            // índice de la lista anterior (si se requiere en remove())...
            private int last_entry;

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /**
             * Determina si hay al menos un elemento en la vista que no haya
             * sido retornado por next().
             *
             * @return un True si hay un valor siguiente (Para mostrar) y False si se terminó de recorrer
             */
            @Override
            public boolean hasNext() {
                Object[] table = TSBHashTableDA.this.table;
                //Controla si está vacía
                if (TSBHashTableDA.this.isEmpty()) {
                    return false;
                }
                //Controla que la posición actual no sobrepase el largo de la tabla
                if (current_entry >= table.length) {
                    return false;
                }
                int next_entry = current_entry + 1;
                for (int i = next_entry; i < table.length; i++) {
                    Entry<K, V> entry = (Entry<K, V>) table[i];
                    if (entry.getState() == CLOSED) return true;
                }
                return false;
            }

            /**
             * Busca la siguiente entrada que esté ocupada por un objeto
             *
             * @return el valor que encontre en el estado CLOSED
             */
            @Override
            public V next() {
                // control: fail-fast iterator...
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): No existe el elemento pedido");
                }

                int next_entry = current_entry;
                next_entry++;
                while (((Entry<K, V>) TSBHashTableDA.this.table[next_entry]).getState() != CLOSED) next_entry++;
                last_entry = current_entry;
                current_entry = next_entry;
                next_ok = true;

                Entry<K, V> entry = (Entry<K, V>) TSBHashTableDA.this.table[current_entry];

                return entry.getValue();
            }

            /**
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * solo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                Entry<K, V> garbage = (Entry<K, V>) TSBHashTableDA.this.table[current_entry];
                garbage.setState(2);
                garbage.setValue(null);

                // quedar apuntando al anterior al que se retornó...
                if (last_entry != current_entry) {
                    current_entry = last_entry;
                }

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // La tabla tiene un elemento menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator:
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }


    }

    /**
     * Es un método que se asegura que el numero introducido sea un primo
     * @param number el número que se desea verificar
     * @return True si el numero es primo y False si el numero no lo es
     */
    public boolean isPrime(int number) {
        if (number % 2 == 0) {
            return false;
        } else {
            for (int i = 3; i < Math.sqrt(number)  ; i+=2) {
                if (number % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Busca el siguiente primo a partir de un número
     *
     * @param n un entero que deseamos saber el siguiente primo
     * @return el siguiente primo a partir de n
     */
    public int nextPrime(int n) {
        if (n % 2 == 0) {
            ++n;
        }
        while (!this.isPrime(n)) {
            n += 2;
        }
        return n;
    }

    /**
     * Es el método de búsqueda de entradas no nulas
     *
     * @param key la llave que posee ese objeto
     * @param ik  el indice en donde se encuentra
     * @return una entrada Map.Entry con esos valores
     */
    private java.util.Map.Entry search_for_entry(K key, int ik) {
        int pos = this.search_for_index(key, ik);
        return pos != -1 ? (Entry) this.table[pos] : null;
    }

    /**
     * @param key
     * @param ik
     * @return
     */
    private int search_for_index(K key, int ik) {

        for (int j = 0; ; j++) {
            ik += (int) Math.pow(j, 2.0D);
            ik %= this.table.length;
            Entry<K, V> entry = (Entry<K, V>) this.table[ik];
            if (entry.getState() == OPEN) {
                return -1;
            }
            if (key.equals(entry.getKey())) {
                return ik;
            }
        }
    }

    /**
     * @param t
     * @param ik
     * @return
     */
    private int search_for_OPEN(Object[] t, int ik) {
        int j = 0;

        while (true) {
            ik += (int) Math.pow(j, 2.0D);
            ik %= t.length;
            TSBHashTableDA<K, V>.Entry<K, V> entry = (TSBHashTableDA.Entry) t[ik];
            if (entry.getState() == 0) {
                return ik;
            }
            ++j;
        }
    }

    /**
     * Hace el calculo de la capacidad en la cual se encuentra el vector
     *
     * @return un float con dicho porcentaje
     */
    private float load_level() {
        return (float) this.count / this.table.length;
    }


    /**
     * @param key
     * @param valor
     * @return
     */
    public V put(K key, V valor) {
        if (key != null && valor != null) {
            int ik = this.h(key);
            V old = null;

            java.util.Map.Entry<K, V> x = this.search_for_entry(key, ik);
            if (x != null){
                old = x.getValue();
                int cont = (int)old+1;
                Entry e = new Entry(key,cont);
                x.setValue((V)e.getValue());
            } else {

                if (this.load_level() >= this.load_factor) {
                    this.rehash();
                }
                int pos = this.search_for_OPEN(this.table, this.h(key));
                java.util.Map.Entry<K, V> entry = new TSBHashTableDA.Entry(key, valor, 1);
                this.table[pos] = entry;
                ++this.count;
                ++this.modCount;
            }

            return old;
        } else {
            throw new NullPointerException("Error: Parámetro nulo");
        }
    }

    /**
     * @param key
     * @return
     */
    public V remove(Object key) {
        if (key == null) throw new NullPointerException("remove(): parámetro null");
        int ik = this.h((K) key);
        V old = null;
        java.util.Map.Entry<K, V> x = this.search_for_entry((K) key, ik);
        if (x != null) {
            old = x.getValue();
            Map.Entry<K, V> y = new TSBHashTableDA.Entry((K) key, null, TOMB);
            this.table[ik] = y;
            this.count--;
            this.modCount--;
        }
        return old;

    }

    /**
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Map)) {
            return false;
        }

        Map<K, V> t = (Map<K, V>) obj;
        if (t.size() != this.size()) {
            return false;
        }
        try {
            Iterator<Map.Entry<K, V>> i = this.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (t.get(key) == null) {
                    return false;
                } else {
                    if (!value.equals(t.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
        return true;
    }

    /**
     * @param value
     * @return
     */
    public boolean contains(Object value) {
        if (value == null) return false;

        Iterator<Map.Entry<K, V>> it = this.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            if (value.equals(entry.getValue())) return true;

        }
        return false;
    }

    /**
     * @param k
     * @return
     */
    private int h(int k) {
        return this.h(k, this.table.length);
    }

    private int h(K key) {
        return this.h(key.hashCode(), this.table.length);
    }

    private int h(K key, int t) {
        return this.h(key.hashCode(), t);
    }

    private int h(int k, int t) {
        if (k < 0) {
            k *= -1;
        }

        return k % t;
    }

    /**
     * Retorna un hash code para la tabla completa.
     * @return un hash code para la tabla.
     */
    @Override
    public int hashCode()
    {
        if(this.isEmpty()) return 0;

        /**
         * En este caso es recomendable usar la función Arrays.hashCode porque
         * garantiza un hash unicos para cada array.
         * Si se usa la suma, los objetos "ab" y "ba" tendrían el mismo hash.
         */
        return Arrays.hashCode(this.table);
    }




    /**
     *
     */
    protected void rehash() {
        Object[] oldTable = this.table;
        int length = this.nextPrime(this.table.length * 2 + 1 );
        this.table = new Object[length];
        for (int i = 0; i < table.length; i++) {
            table[i] = new Entry<K,V>(null,null);
            //Esto llama al constructor de OPEN de Entry
        }
        this.count = 0;
        // qeu no pase el limite

        for(int i = 0; i < oldTable.length; ++i) {
            TSBHashTableDA.Entry oldEntry = (TSBHashTableDA.Entry)oldTable[i];
            if (oldEntry.getState() == CLOSED)

                this.put((K)oldEntry.getKey(), (V)oldEntry.getValue());
        }

    }



    public int size() {
        return (this.count);
    }

    public boolean isEmpty() {
        return (this.count == 0) ;
    }

    public boolean containsKey(Object key) {
        return (this.get((K)key) != null);
    }

    public boolean containsValue(Object value) {
        return this.contains(value);
    }

    public V get(Object key) {
        if (key == null) throw new NullPointerException("get(): parámetro null");
        int ib = this.h((K)key);
        V old = null;
        java.util.Map.Entry<K, V> x = this.search_for_entry((K)key, ib);
        return (x != null)? x.getValue() : null;

    }

    /**
     *
     * @param map
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        for(Map.Entry<? extends K, ? extends V> e : map.entrySet())
        {
            put(e.getKey(), e.getValue());
        }

    }

    /**
     *
     */
    public void clear()
    {
        this.table = new Object[this.initial_capacity];
        for (int i = 0; i < table.length; i++) {
            table[i] = new Entry<K,V>(null,null);
            //Esto llama al constructor de OPEN de Entry
        }
        this.count = 0;
        this.modCount++;

    }


    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }

        return keySet;
    }


    public Collection<V> values() {
        if (valores == null) {
            valores = new ValueCollection();
        }

        return valores;
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        if(entrySet == null)
        {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    protected Object clone() { //throws CloneNotSupportedException {
        try {
        TSBHashTableDA<K, V> t = (TSBHashTableDA<K, V>)super.clone();

        for(Map.Entry<K,V> entry : this.entrySet()){
            t.put(entry.getKey(),entry.getValue());
        }
        t.keySet = null;
        t.entrySet = null;
        t.valores = null;
        t.modCount = 0;
        return t;
        }
        catch(CloneNotSupportedException exception){
            exception.printStackTrace();
        }
        return null;
    }
}