package clases;

/**
 * Una clase con un main() simple para probar la clase TSBHashtableDA.
 * @author Ing. Valerio Frittelli.
 * @version Octubre de 2017.
 */
public class Test 
{
    public static void main(String args[])
    {
        // una tabla "corta" con factor de carga pequeño...
        TSBHashTableDA<Integer, String> ht1 = new TSBHashTableDA<>(11, 0.2f);
        System.out.println("Contenido inicial: " + ht1);
        
        // algunas inserciones...
        ht1.put(1, "Argentina");
        ht1.put(2, "Brasil");
        ht1.put(3, "Chile");
        ht1.put(4, "Mexico");
        ht1.put(5, "Uruguay");
        ht1.put(6, "Perú");
        ht1.put(7, "Colombia");
        ht1.put(8, "Ecuador");
        ht1.put(9, "Paraguay");
        ht1.put(10, "Bolivia");
        ht1.put(11, "Venezuela");
        ht1.put(12, "Estados Unidos");
        System.out.println("Luego de algunas inserciones: " + ht1);
        System.out.println("Buscar key Brasil ");
        System.out.println(ht1.get(12));
        System.out.println("Objeto borrado "+ht1.remove(10));
        System.out.println("Luego del borrado: " + ht1);
        try {
            TSBHashTableDA<Integer, String> ht2 = (TSBHashTableDA<Integer, String>) ht1.clone();
            ht2.put(1, "Bolivia");
            System.out.println("Segunda tabla: " + ht2);
            System.out.println(ht1.equals(ht2));
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        System.out.println("Contiene value Brasil?: " + ht1.contains("Brasil"));
        System.out.println("Contiene: " + ht1);




        //probar el remove anda
        //probar el rehash anda
        //check estado de entries
        // resolver clone + put all (parcialmente resuleto)
        // borrar mod count
        // key value no debe ser null
        /*


        try{
        TSBHashtableDA<Integer, String> ht2 = (TSBHashtableDA<Integer, String>) ht1.clone();
        System.out.println("Segunda tabla: " + ht2);}
        catch(CloneNotSupportedException exception){
            exception.printStackTrace();
        }
        /*
        ///////////////////////////////////////////

        System.out.println("Tabla 1 recorrida a partir de una vista: ");
        Set<Map.Entry<Integer, String>> se = ht1.entrySet();
        Iterator<Map.Entry<Integer, String>> it = se.iterator();
        while(it.hasNext())
        {
            Map.Entry<Integer, String> entry = it.next();
            System.out.println("Par: " + entry);
        }

         */

        /*
        check
        Definir dentro de la clase TSBHashTableDA las tres clases internas para gestionar las
vistas stateless de claves, de valores y de pares de la tabla, incluyendo a su vez en ellas
las clases internas para representar a los iteradores asociados a cada vista.
         */
    }
}
