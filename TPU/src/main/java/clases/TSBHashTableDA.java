package clases;

import java.io.Serializable;
import java.util.*;


/**
 * Clase para emular la funcionalidad de la clase java.util.Hashtable, pero implementada
 * en base al modelo de Resolución de Colisiones por Direccionamiento Abierto. Modelo para
 * aplicar de base para el desarrollo del TPU.
 * 
 * Tecnología de Software de Base - Trabajo Práctico Único [Etapa 1]
 * Tablas Hash (Implementación por Direccionamiento Abierto)
 * 
 * @author Grupo 31 - Falco, Gonzalo - Marzo, Lautaro - Pesetti, Sabrina - Vela, Azul
 * @version Octubre de 2022.
 * @param <K> el tipo de los objetos que serán usados como clave en la tabla.
 * @param <V> el tipo de los objetos que serán los valores de la tabla.
 */
public class TSBHashTableDA<K,V> implements Map<K,V>, Cloneable, Serializable
{
    //************************ Constantes (privadas o públicas).

    // estados en los que puede estar una casilla o slot de la tabla...
    public static final int OPEN = 0;
    public static final int CLOSED = 1;
    public static final int TOMBSTONE = 2;

    //************************ Atributos privados (estructurales).

    // la tabla hash: el arreglo que contiene todos los objetos...
    // private Map.Entry<K, V> table[]; //sabri
    private Object table[];

    // el tamaño inicial de la tabla (tamaño con el que fue creada)...
    private int initial_capacity;

    // la cantidad de objetos que contiene la tabla...
    private int count;

    // el factor de carga para calcular si hace falta un rehashing...
    private float load_factor;


    //************************ Atributos privados (para gestionar las vistas).

    /*
     * (Tal cual están definidos en la clase java.util.Hashtable)
     * Cada uno de estos campos se inicializa para contener una instancia de la
     * vista que sea más apropiada, la primera vez que esa vista es requerida.
     * Las vistas son objetos stateless (no se requiere que almacenen datos, sino
     * que sólo soportan operaciones), y por lo tanto no es necesario crear más
     * de una de cada una.
     */
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;


    //************************ Atributos protegidos (control de iteración).

    // conteo de operaciones de cambio de tamaño (fail-fast iterator).
    protected transient int modCount;


    //************************ Constructores.

    /**
     * Crea una tabla vacía, con la capacidad inicial igual a 11 y con factor
     * de carga igual a 0.5f (que equivale a un nivel de carga del 50%).
     */
    public TSBHashTableDA()
    {
        this(11, 0.5f);
    }

    /**
     * Crea una tabla vacía, con la capacidad inicial indicada y con factor
     * de carga igual a 0.5f (que equivale a un nivel de carga del 50%).
     * @param initial_capacity la capacidad inicial de la tabla.
     */
    public TSBHashTableDA(int initial_capacity)
    {
        this(initial_capacity, 0.5f);
    }

    /**
     * Crea una tabla vacía, con la capacidad inicial indicada y con el factor
     * de carga indicado. Si la capacidad inicial indicada por initial_capacity
     * es menor o igual a 0, la tabla será creada de tamaño 11. Si el factor de
     * carga indicado es negativo, cero o mayor a 0.5, se ajustará a 0.5f. Si el
     * valor de initial_capacity no es primo, el tamaño se ajustará al primer
     * primo que sea mayor a initial_capacity.
     * @param initial_capacity la capacidad inicial de la tabla.
     * @param load_factor el factor de carga de la tabla.
     */
    public TSBHashTableDA(int initial_capacity, float load_factor)
    {
        if(load_factor <= 0 || load_factor > 0.5) { load_factor = 0.5f; }
        if(initial_capacity <= 0) { initial_capacity = 11; }
        else
        {
            if(!isPrime(initial_capacity))
            {
                initial_capacity = nextPrime(initial_capacity);
            }
        }

        //this.table = new Map.Entry[initial_capacity]; //sabri
        this.table = new Object[initial_capacity];
        for(int i=0; i<table.length; i++)
        {
            table[i] = new Entry<K, V>(null, null);
        }

        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;
    }

    /**
     * Crea una tabla a partir del contenido del Map especificado.
     * @param t el Map a partir del cual se creará la tabla.
     */
    public TSBHashTableDA(Map<? extends K,? extends V> t)
    {
        this(11, 0.5f);
        this.putAll(t);
    }


    //************************ Implementación de métodos especificados por Map.

    /**
     * Retorna la cantidad de elementos contenidos en la tabla.
     * @return la cantidad de elementos de la tabla.
     */
    @Override
    public int size()
    {
        return this.count;
    }

    /**
     * Determina si la tabla está vacía (no contiene ningún elemento).
     * @return true si la tabla está vacía.
     */
    @Override
    public boolean isEmpty()
    {
        return (this.count == 0);
    }

    /**
     * Determina si la clave key está en la tabla.
     * @param key la clave a verificar.
     * @return true si la clave está en la tabla.
     * @throws NullPointerException si la clave es null.
     */
    @Override
    public boolean containsKey(Object key)
    {
        return (this.get((K)key) != null);
    }

    /**
     * Determina si alguna clave de la tabla está asociada al objeto value que
     * entra como parámetro. Equivale a contains().
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave está asociada efectivamente a ese value.
     */
    @Override
    public boolean containsValue(Object value)
    {
        return this.contains(value);
    }

    /**
     * Retorna el objeto al cual está asociada la clave key en la tabla, o null
     * si la tabla no contiene ningún objeto asociado a esa clave.
     * @param key la clave que será buscada en la tabla.
     * @return el objeto asociado a la clave especificada (si existe la clave) o
     *         null (si no existe la clave en esta tabla).
     * @throws NullPointerException si key es null.
     * @throws ClassCastException si la clase de key no es compatible con la
     *         tabla.
     */
    @Override
    public V get(Object key) throws ClassCastException, NullPointerException
    {
        if(key == null)
            throw new NullPointerException("get(): parámetro null");

        //calculo el indice
        int mi = this.h((K) key);
        Map.Entry<K,V> aux = search_for_entry((K) key, mi);
        if (aux != null)
            return aux.getValue();
        return null;
    }

    /**
     * Asocia el valor (value) especificado, con la clave (key) especificada en
     * esta tabla. Si la tabla contenía previamente un valor asociado para la
     * clave, entonces el valor anterior es reemplazado por el nuevo (y en este
     * caso el tamaño de la tabla no cambia).
     * @param key la clave del objeto que se quiere agregar a la tabla.
     * @param value el objeto que se quiere agregar a la tabla.
     * @return el objeto anteriormente asociado a la clave si la clave ya
     *         estaba asociada con alguno, o null si la clave no estaba antes
     *         asociada a ningún objeto.
     * @throws NullPointerException si key es null o value es null.
     */
    @Override
    public V put(K key, V value)
    {

        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        int ik = this.h(key);

        V old = null;
        Map.Entry<K, V> x = this.search_for_entry((K)key, ik);
        if(x != null)
        {
            old = x.getValue();
            x.setValue(value);
        }
        else
        {
            if(this.load_level() >= this.load_factor) { this.rehash(); }
            int pos = search_for_OPEN(this.table, this.h(key));
            Map.Entry<K, V> entry = new Entry<>(key, value, CLOSED);
            table[pos] = entry;

            this.count++;
            this.modCount++;
        }

        return old;
    }

    /**
     * Elimina de la tabla la clave key (y su correspondiente valor asociado).
     * El método no hace nada si la clave no está en la tabla.
     * @param key la clave a eliminar.
     * @return El objeto al cual la clave estaba asociada, o null si la clave no
     *         estaba en la tabla.
     * @throws NullPointerException - if the key is null.
     */
    //@Override
    //public V remove(Object key)
    //@Override
    public boolean remove(Object key, Object value) {
        return this.remove(key)!=null;
    }

    @Override
    public V remove(Object key){
        if(key == null)
            throw new NullPointerException("remove(): parámetro null");

        int mi = this.h((K) key);
        V old = null;

        int pos  = search_for_index((K) key, mi);
        if (pos != -1)
        {
            Entry<K,V> aux = ((Entry)table[pos]);
            old = aux.getValue();
            ((Entry)table[pos]).setState(TOMBSTONE);
            count--;
            modCount++;
            return (V) old; //sabri
        }
        //return (V) old;
        return null;//sabri
    }

    /**
     * Copia en esta tabla, todos los objetos contenidos en el map especificado.
     * Los nuevos objetos reemplazarán a los que ya existan en la tabla
     * asociados a las mismas claves (si se repitiese alguna).
     * @param m el map cuyos objetos serán copiados en esta tabla.
     * @throws NullPointerException si m es null.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {

        if (m == null)
        {
            throw new NullPointerException("El parámetro no puede ser null");
        }
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet())
        {
            if(e!=null) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Elimina el contenido de la tabla, de forma de dejarla vacía. En esta
     * implementación además, el arreglo de soporte vuelve a tener el tamaño que
     * inicialmente tuvo al ser creado el objeto.
     */
    @Override
    public void clear()
    {
        this.table = new Object[initial_capacity];
        for(int i=0; i<table.length; i++)
        {
            table[i]= new Entry<K, V>(null, null);
        }
        this.count = 0;
        this.modCount = 0;
    }

    /**
     * Retorna un Set (conjunto) a modo de vista de todas las claves (key)
     * contenidas en la tabla. El conjunto está respaldado por la tabla, por lo
     * que los cambios realizados en la tabla serán reflejados en el conjunto, y
     * viceversa. Si la tabla es modificada mientras un iterador está actuando
     * sobre el conjunto vista, el resultado de la iteración será indefinido
     * (salvo que la modificación sea realizada por la operación remove() propia
     * del iterador, o por la operación setValue() realizada sobre una entrada
     * de la tabla que haya sido retornada por el iterador). El conjunto vista
     * provee métodos para eliminar elementos, y esos métodos a su vez
     * eliminan el correspondiente par (key, value) de la tabla (a través de las
     * operaciones Iterator.remove(), Set.remove(), removeAll(), retainAll()
     * y clear()). El conjunto vista no soporta las operaciones add() y
     * addAll() (si se las invoca, se lanzará una UnsuportedOperationException).
     * @return un conjunto (un Set) a modo de vista de todas las claves
     *         mapeadas en la tabla.
     */
    @Override
    public Set<K> keySet()
    {
        if(keySet == null)
        {
            // keySet = Collections.synchronizedSet(new KeySet());
            keySet = new KeySet();
        }
        return keySet;
    }

    /**
     * Retorna una Collection (colección) a modo de vista de todos los valores
     * (values) contenidos en la tabla. La colección está respaldada por la
     * tabla, por lo que los cambios realizados en la tabla serán reflejados en
     * la colección, y viceversa. Si la tabla es modificada mientras un iterador
     * está actuando sobre la colección vista, el resultado de la iteración será
     * indefinido (salvo que la modificación sea realizada por la operación
     * remove() propia del iterador, o por la operación setValue() realizada
     * sobre una entrada de la tabla que haya sido retornada por el iterador).
     * La colección vista provee métodos para eliminar elementos, y esos métodos
     * a su vez eliminan el correspondiente par (key, value) de la tabla (a
     * través de las operaciones Iterator.remove(), Collection.remove(),
     * removeAll(), removeAll(), retainAll() y clear()). La colección vista no
     * soporta las operaciones add() y addAll() (si se las invoca, se lanzará
     * una UnsuportedOperationException).
     * @return una colección (un Collection) a modo de vista de todas los
     *         valores mapeados en la tabla.
     */
    @Override
    public Collection<V> values()
    {
        if(values==null)
        {
            // values = Collections.synchronizedCollection(new ValueCollection());
            values = new ValueCollection();
        }
        return values;
    }

    /**
     * Retorna un Set (conjunto) a modo de vista de todos los pares (key, value)
     * contenidos en la tabla. El conjunto está respaldado por la tabla, por lo
     * que los cambios realizados en la tabla serán reflejados en el conjunto, y
     * viceversa. Si la tabla es modificada mientras un iterador está actuando
     * sobre el conjunto vista, el resultado de la iteración será indefinido
     * (salvo que la modificación sea realizada por la operación remove() propia
     * del iterador, o por la operación setValue() realizada sobre una entrada
     * de la tabla que haya sido retornada por el iterador). El conjunto vista
     * provee métodos para eliminar elementos, y esos métodos a su vez
     * eliminan el correspondiente par (key, value) de la tabla (a través de las
     * operaciones Iterator.remove(), Set.remove(), removeAll(), retainAll()
     * and clear()). El conjunto vista no soporta las operaciones add() y
     * addAll() (si se las invoca, se lanzará una UnsuportedOperationException).
     * @return un conjunto (un Set) a modo de vista de todos los objetos
     *         mapeados en la tabla.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        if(entrySet == null)
        {
            // entrySet = Collections.synchronizedSet(new EntrySet());
            entrySet = new EntrySet();
        }
        return entrySet;
    }


    //************************ Redefinición de métodos heredados desde Object.

    /**
     * Retorna una copia superficial de la tabla. Las listas de desborde o
     * buckets que conforman la tabla se clonan ellas mismas, pero no se clonan
     * los objetos que esas listas contienen: en cada bucket de la tabla se
     * almacenan las direcciones de los mismos objetos que contiene la original.
     * @return una copia superficial de la tabla.
     * @throws java.lang.CloneNotSupportedException si la clase no implementa la
     *         interface Cloneable.
     */
    @Override
    protected Object clone() //throws CloneNotSupportedException
    {
        try {
            TSBHashTableDA<K, V> t = (TSBHashTableDA<K, V>) super.clone();
            t.table = new Object[this.table.length];
            //t.table = new Object[this.table.length];
            //t.putAll(this);
            for (int i = 0; i < this.table.length; i++) {
                t.table[i] = this.table[i];
            }
            t.keySet = null;
            t.entrySet = null;
            t.values = null;
            t.modCount = 0;
            return t;
        }
        catch(CloneNotSupportedException exception){
            exception.printStackTrace();
        }
        return null;
    }
    /*
        /**
         * Determina si esta tabla es igual al objeto especificado.
         * @param obj el objeto a comparar con esta tabla.
         * @return true si los objetos son iguales.
         */
    /*
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext())
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                // hash
                if(t.get(key) == null) { return false; }
                else
                {
                    if(!value.equals(t.get(key))) { return false; } //agregar comparación de hashcode
                }
            }
        }

        catch (ClassCastException | NullPointerException e)
        {
            System.out.println("Acá entró al catch");
            e.printStackTrace();
            //return false;
        }

        return true;
    }
*/
    /**
     * Retorna un hash code para la tabla completa.
     * @return un hash code para la tabla.
     */
    /*
    @Override
    public int hashCode()
    {
        // HACER...
        int hc = super.hashCode();
        return hc;
    }
*/
    @Override
    public int hashCode()
    {
        int h = 0;
        Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
        while(i.hasNext())
        {
            h += i.next().hashCode();
        }
        return h;
    }


    /**
     * Devuelve el contenido de la tabla en forma de String.
     * @return una cadena con el contenido completo de la tabla.
     */
    @Override
    public String toString()
    {
        // REVISAR... Asegúrense de que funciona bien...
        StringBuilder cad = new StringBuilder("[");
        for(int i = 0; i < this.table.length; i++)
        {
            Entry<K, V> entry = (Entry<K, V>) table[i];
            if(entry.getState() == CLOSED)
            {
                cad.append(entry.toString());
                cad.append(" ");
            }
        }
        cad.append("]");
        return cad.toString();
    }


    //************************ Métodos específicos de la clase.


    /**
     * Determina si alguna clave de la tabla está asociada al objeto value que
     * entra como parámetro. Equivale a containsValue().
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave está asociada efectivamente a ese value.
     */

    public boolean contains(Object value) {
        if(value == null) return false;
        System.out.println("Método contains");
        for (int i = 0; i < this.table.length; i++)
        {
            Entry<K, V> entry = (Entry<K, V>) this.table[i];
            if(entry.getState()==CLOSED){
                if(value==entry.getValue()) return true;}
        }
        return false;
    }
    /*
    public boolean contains(Object value)
    {
        if (value == null)
        {
            throw new NullPointerException("El parametro no puede ser null.");
        }

        Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
        while(i.hasNext())
        {
            Map.Entry<K, V> e = i.next();
            if(e!=null){
            V val = e.getValue();
            if(val.equals(value)) return true;}
        }
        return false;
    }
    /*
    @Override
    public boolean contains(Object o)
    {
        if(o == null) { return false; }
        if(!((Entry) o instanceof Entry)) { return false; }

        Entry<K, V> entry = (Entry<K,V>)o;
        K key = entry.getKey();
        Object t[] = TSBHashTableDA.this.table;

        for (int i = 0; i < t.length; i++) {
            Entry<K, V> x = (Entry<K,V>)t[i];
            if (key.equals(x.getKey())) {
                if (x.estado == 1) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }*/

    /**
     * Incrementa el tamaño de la tabla y reorganiza su contenido. Se invoca
     * automaticamente cuando se detecta que la cantidad promedio de nodos por
     * lista supera a cierto el valor critico dado por (10 * load_factor). Si el
     * valor de load_factor es 0.8, esto implica que el límite antes de invocar
     * rehash es de 8 nodos por lista en promedio, aunque seria aceptable hasta
     * unos 10 nodos por lista.
     */
    protected void rehash()
    {
        int old_length = this.table.length;

        // nuevo tamaño: primer primo mayor o igual al 50% del anterior...
        int new_length = nextPrime((int)(old_length * 1.5f));

        // crear el nuevo arreglo de tamaño new_length...
        Object temp[] = new Object[new_length];
        for(int j=0; j<temp.length; j++) { temp[j] = new Entry<>(null, null); }

        // notificación fail-fast iterator... la tabla cambió su estructura...
        this.modCount++;

        // recorrer el viejo arreglo y redistribuir los objetos que tenia...
        for(int i=0; i<this.table.length; i++)
        {
            // obtener un objeto de la vieja lista...
            Entry<K, V> x = (Entry<K, V>) table[i];

            // si la casilla está cerrada...
            if(x.getState() == CLOSED)
            {
                // ...obtener el valor de dispersión en el nuevo arreglo...
                K key = x.getKey();
                int ik = this.h(key, temp.length);
                int y = search_for_OPEN(temp, ik);

                // ...insertar en el nuevo arreglo
                temp[y] = x;
            }
        }

        // cambiar la referencia table para que apunte a temp...
        this.table = temp;
    }


    //************************ Métodos privados.

    /*
     * Función hash. Toma una clave entera k y calcula y retorna un índice
     * válido para esa clave para entrar en la tabla.
     */
    private int h(int k)
    {
        return h(k, this.table.length);
    }

    /*
     * Función hash. Toma un objeto key que representa una clave y calcula y
     * retorna un índice válido para esa clave para entrar en la tabla.
     */
    private int h(K key)
    {
        return h(key.hashCode(), this.table.length);
    }

    /*
     * Función hash. Toma un objeto key que representa una clave y un tamaño de
     * tabla t, y calcula y retorna un índice válido para esa clave dedo ese
     * tamaño.
     */
    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }

    /*
     * Función hash. Toma una clave entera k y un tamaño de tabla t, y calcula y
     * retorna un índice válido para esa clave dado ese tamaño.
     */
    private int h(int k, int t)
    {
        if(k < 0) k *= -1;
        return k % t;
    }

    private boolean isPrime(int n)
    {
        // negativos no admitidos en este contexto...
        if(n < 0) return false;

        if(n == 1) return false;
        if(n == 2) return true;
        if(n % 2 == 0) return false;

        int raiz = (int) Math.pow(n, 0.5);
        for(int div = 3;  div <= raiz; div += 2)
        {
            if(n % div == 0) return false;
        }

        return true;
    }

    private int nextPrime (int n)
    {
        if(n % 2 == 0) n++;
        for(; !isPrime(n); n+=2);
        return n;
    }

    /**
     * Calcula el nivel de carga de la tabla, como un número en coma flotante entre 0 y 1.
     * Si este valor se multiplica por 100, el resultado es el porcentaje de ocupación de la
     * tabla.
     * @return el nivel de ocupación de la tabla.
     */
    private float load_level()
    {
        return (float) this.count / this.table.length;
    }

    /*
     * Busca en la tabla un objeto Entry cuya clave coincida con key, a partir
     * de la posición ik. Si lo encuentra, retorna ese objeto Entry. Si no lo
     * encuentra, retorna null. Aplica exploración cuadrática.
     */
    private Map.Entry<K, V> search_for_entry(K key, int ik)
    {
        int pos = search_for_index(key, ik);
        return pos != -1 ? (Map.Entry<K, V>) table[pos] : null;
    }

    /*
     * Busca en la tabla un objeto Entry cuya clave coincida con key, a partir
     * de la posición ik. Si lo encuentra, retorna su posicíón. Si no lo encuentra,
     * retorna -1. Aplica exploración cuadrática.
     */
    private int search_for_index(K key, int ik)
    {
        for(int j=0; ;j++)
        {
            int y = ik + (int)Math.pow(j, 2);
            y %= table.length;

            Entry<K, V> entry = (Entry<K, V>) table[y];
            if(entry.getState() == OPEN || entry.getState() == TOMBSTONE) { return -1; } //check sabri
            if(key.equals(entry.getKey())) { return y; }
        }
    }

    /*
     * Retorna el índice de la primera casilla abierta, a partir de la posición ik,
     * en la tabla t. Aplica exploración cuadrática.
     */
    private int search_for_OPEN(Object t[], int ik)
    {
        for(int j=0; ;j++)
        {
            int y = ik + (int)Math.pow(j, 2);
            y %= t.length;

            Entry<K, V> entry = (Entry<K, V>) t[y];
            if(entry.getState() == OPEN) { return y; }
        }
    }

    //************************ Clases Internas.

    /*
     * Clase interna que representa los pares de objetos que se almacenan en la
     * tabla hash: son instancias de esta clase las que realmente se guardan en
     * en cada una de las listas del arreglo table que se usa como soporte de
     * la tabla. Lanzará una IllegalArgumentException si alguno de los dos
     * parámetros es null.
     */
    private class Entry<K, V> implements Map.Entry<K, V>
    {
        private K key;
        private V value;
        private int state;

        public Entry(K key, V value)
        {
            this(key, value, OPEN);
        }

        public Entry(K key, V value, int state)
        {
            this.key = key;
            this.value = value;
            this.state = state;
        }

        public Entry() {
            this.state=OPEN;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        public int getState() { return state; }

        @Override
        public V setValue(V value)
        {
            if(value == null)
            {
                throw new IllegalArgumentException("setValue(): parámetro null...");
            }

            V old = this.value;
            this.value = value;
            return old;
        }

        public void setState(int ns)
        {
            if(ns >= 0 && ns < 3)
            {
                state = ns;
            }
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }

            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }
            if (this.hashCode() != other.hashCode()) { return false; }
            return true;
        }

        @Override
        public String toString()
        {
            return "(" + key.toString() + ", " + value.toString() + ")";
        }
    }

    /*
     * Clase interna que representa una vista de todas los Claves mapeadas en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class KeySet extends AbstractSet<K>
    {
        @Override
        public Iterator<K> iterator()
        {
            return new KeySetIterator();
        }

        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o)
        {
            return (TSBHashTableDA.this.remove(o) != null);
        }

        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class KeySetIterator implements Iterator<K>
        {
            // REVISAR y HACER... Agregar los atributos que necesiten...

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            private int current_entry;
            private int last_entry;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public KeySetIterator()
            {
                // HACER...
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
                current_entry = 0;
                last_entry = 0;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext() {
                Object table[] = TSBHashTableDA.this.table;

                if (TSBHashTableDA.this.isEmpty()) {
                    return false;
                }
                if (current_entry >= table.length) {
                    return false;
                }

                Entry<K, V> entry = (Entry<K, V>) table[current_entry];
                if ( entry.getState() == TOMBSTONE) {
                    int next_entry = current_entry + 1;
                    entry = (Entry<K, V>) table[next_entry];
                    while (next_entry < table.length-1 && (entry.getState() == CLOSED || entry.getState() == OPEN)) {
                        next_entry++;
                        entry = (Entry<K, V>) table[next_entry];
                    }
                    if (next_entry == table.length-1) {
                        return false;
                    }
                }
                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public K next() {
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificacion inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                Object table[] = TSBHashTableDA.this.table;

                Entry<K, V> entry = (Entry<K, V>) table[current_entry];
                int next = current_entry;
                if (entry.getState() == OPEN || entry.getState() == CLOSED) {
                    last_entry = current_entry;
                    next++;
                    entry = (Entry<K, V>) table[next];
                    while (entry.getState() == OPEN || entry.getState() == CLOSED) {
                        next++;
                        entry = (Entry<K, V>) table[next];
                    }
                }

                if (next==current_entry) {
                    next++;
                    entry = (Entry<K, V>) table[next];
                    while (entry.getState() == OPEN || entry.getState() == CLOSED) {
                        next++;
                        entry = (Entry<K, V>) table[next];
                    }
                }

                next_ok = true;

                current_entry = next;
                entry = (Entry<K, V>) table[current_entry];
                K key = entry.getKey();
                return key;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                // REVISAR Y HACER...

                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                TSBHashTableDA.this.table[current_entry] = new Entry<>();

                if (last_entry != current_entry) {
                    current_entry = last_entry;
                }

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator...
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /*
     * Clase interna que representa una vista de todos los PARES mapeados en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {

        @Override
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new EntrySetIterator();
        }

        /*
         * Verifica si esta vista (y por lo tanto la tabla) contiene al par
         * que entra como parámetro (que debe ser de la clase Entry).
         */
        @Override
        public boolean contains(Object o)
        {
            if(o == null) { return false; }
            if(!(o instanceof Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K,V>)o;
            K key = entry.getKey();
            //int index = TSBHashTableDA.this.h(key);
            if(TSBHashTableDA.this.containsKey(key)){
                V val = TSBHashTableDA.this.get(key);
                return val==entry.getValue();
            }

            return false;
        }

        /*
         * Elimina de esta vista (y por lo tanto de la tabla) al par que entra
         * como parámetro (y que debe ser de tipo Entry).
         */
        @Override
        public boolean remove(Object o)
        {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();
            if(TSBHashTableDA.this.remove(key)!=null)
            {
                TSBHashTableDA.this.count--;
                TSBHashTableDA.this.modCount++;
                return true;
            }
            return false;
        }

        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>>
        {
            // REVISAR y HACER... Agregar los atributos que necesiten...

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            private int current_entry;
            private int last_entry;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public EntrySetIterator()
            {
                current_entry = 0;
                last_entry = 0;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                // variable auxiliar t para simplificar accesos...
                Object table[] =  TSBHashTableDA.this.table;
                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(current_entry >= table.length) { return false; }

                Entry<K, V> entry = (Entry<K, V>) table[current_entry];
                if ( entry.getState() == TOMBSTONE) {
                    int next_entry = current_entry + 1;
                    entry = (Entry<K, V>) table[next_entry];

                    while (next_entry < table.length-1 && (entry.getState() == OPEN || entry.getState() == CLOSED)) {
                        next_entry++;
                        entry = (Entry<K, V>) table[next_entry];
                    }
                    if (next_entry == table.length-1) {
                        return false;
                    }
                }

                return true;
            }


            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public Entry<K, V> next() {
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificacion inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                Object table[] = TSBHashTableDA.this.table;

                Entry<K, V> entry = (Entry<K, V>) table[current_entry];
                int next = current_entry;
                if (entry.getState() == OPEN || entry.getState() == CLOSED){
                    last_entry = current_entry;
                    next++;
                    entry = (Entry<K, V>) table[next];
                    while (entry.getState() == OPEN || entry.getState() == CLOSED){
                        next++;
                        entry = (Entry<K, V>) table[next];
                    }
                }

                if (next==current_entry) {
                    next++;
                    entry = (Entry<K, V>) table[next];
                    while (entry.getState() == OPEN || entry.getState() == CLOSED) {
                        next++;
                        entry = (Entry<K, V>) table[next];
                    }
                }
                next_ok = true;
                current_entry = next;
                entry = (Entry<K, V>) table[current_entry];
                Entry e = entry;
                return e;
            }

            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                //Entry<K, V> garbage = (Entry<K, V>) TSBHashTableDA.this.table[current_entry];
                TSBHashTableDA.this.table[current_entry] = new Entry<>();
                // retomar acá

                if (last_entry != current_entry) {
                    current_entry = last_entry;
                }

                next_ok = false;

                TSBHashTableDA.this.count--;

                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    /*
     * Clase interna que representa una vista de todos los VALORES mapeados en
     * la tabla: si la vista cambia, cambia también la tabla que le da respaldo,
     * y viceversa. La vista es stateless: no mantiene estado alguno (es decir,
     * no contiene datos ella misma, sino que accede y gestiona directamente los
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class ValueCollection extends AbstractCollection<V>
    {
        @Override
        public Iterator<V> iterator()
        {
            return new ValueCollectionIterator();
        }

        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsValue(o);
        }

        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V>
        {
            // REVISAR y HACER... Agregar los atributos que necesiten...

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            private int current_entry;
            private int last_entry;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator()
            {
                // HACER...

                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
                current_entry = 0;
                last_entry = 0;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                Object table[] = TSBHashTableDA.this.table;

                if (TSBHashTableDA.this.isEmpty()) {
                    return false;
                }
                if (current_entry >= table.length) {
                    return false;
                }

                Entry<K, V> entry = (Entry<K, V>) table[current_entry];
                if (entry.getState() == TOMBSTONE) {
                    int next_entry = current_entry + 1;
                    entry = (Entry<K, V>) table[next_entry];
                    while (next_entry < table.length-1 && (entry.getState() == CLOSED || entry.getState() == OPEN)) {
                        next_entry++;
                        entry = (Entry<K, V>) table[next_entry];
                    }
                    if (next_entry == table.length-1) {
                        return false;
                    }
                }

                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public V next(){
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificacion inesperada de tabla en uso...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento solicitado...");
                }

                Object table[] = TSBHashTableDA.this.table;

                Entry<K, V> entry = (Entry<K, V>) table[current_entry];
                int next = current_entry;
                if (entry.getState() == CLOSED || entry.getState() == OPEN) {
                    last_entry = current_entry;

                    next++;
                    entry = (Entry<K, V>) table[next];
                    while (entry.getState() == CLOSED || entry.getState() == OPEN) {
                        next++;
                        entry = (Entry<K, V>) table[next];
                    }
                }

                if (next==current_entry) {
                    next++;
                    entry = (Entry<K, V>) table[next];
                    while (entry.getState() == CLOSED || entry.getState() == OPEN) {
                        next++;
                        entry = (Entry<K, V>) table[next];
                    }
                }

                next_ok = true;

                current_entry = next;
                entry = (Entry<K, V>) table[current_entry];
                V val = entry.getValue();
                return val;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                // HACER...

                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                TSBHashTableDA.this.table[current_entry] = new Entry<>();

                if (last_entry != current_entry) {
                    current_entry = last_entry;
                }
                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator...
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }
}
