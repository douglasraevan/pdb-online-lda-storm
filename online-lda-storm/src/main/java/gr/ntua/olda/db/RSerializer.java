package gr.ntua.olda.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Serializer to persist state to Redis
 * @param <T>
 */
public class RSerializer<T> {

    public String serialize(T ob)
    {
    	String res="";
    	 
    	 try {
    	     ByteArrayOutputStream bo = new ByteArrayOutputStream();
    	     ObjectOutputStream so = new ObjectOutputStream(bo);
    	     so.writeObject(ob);
    	     so.flush();
    	     res = bo.toString("ISO-8859-1");
    	     
    	 } catch (Exception e) {
    	     System.out.println(e);
    	 }
    	 return res;
    }

    public T deserialize(String st)
    {
    	T obj = null;
   	 	
   	 	try {
   	 		byte b[] = st.getBytes("ISO-8859-1"); 
   	 		ByteArrayInputStream bi = new ByteArrayInputStream(b);
   	 		ObjectInputStream si = new ObjectInputStream(bi);       	 		
   	 		obj = (T) si.readObject();
   	 	} 
   	 	catch (Exception e) {
   	 		System.out.println(e);
   	 	}
   	 	return obj;
    }
}
