
package clases;

import java.util.Objects;

public class Student 
{
    private int legajo;
    private String nombre;

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + this.legajo;
        hash = 79 * hash + Objects.hashCode(this.nombre);
        return hash;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Student other = (Student) obj;
        if (this.legajo != other.legajo)
        {
            return false;
        }
        return true;
    }
}
