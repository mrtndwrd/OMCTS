package mrtndwrd;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

public class SerializableTuple<X extends Serializable, Y extends Serializable> 
	extends Tuple<X, Y> implements Serializable
{
	private static final long serialVersionUID = 4238473280574032175L;

	public SerializableTuple()
	{

	}

	public SerializableTuple(X x, Y y)
	{
		super(x, y);
	}
	
	private void readObject(ObjectInputStream aInputStream) 
		throws ClassNotFoundException, IOException 
	{
		//always perform the default de-serialization first
		aInputStream.defaultReadObject();
		x = (X) aInputStream.readObject();
		y = (Y) aInputStream.readObject();
	}

	private void writeObject(ObjectOutputStream aOutputStream)
		throws IOException 
	{
		// perform the default serialization for all non-transient, non-static
		// fields
		aOutputStream.defaultWriteObject();
		aOutputStream.writeObject(x);
		aOutputStream.writeObject(y);
	}
}



