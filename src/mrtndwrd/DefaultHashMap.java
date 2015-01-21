package mrtndwrd;

import java.util.HashMap;

import java.io.FileWriter;

/** Simple DefaultHashMap from
 * https://stackoverflow.com/questions/7519339/hashmap-to-return-default-value-for-non-found-keys
 */
public class DefaultHashMap<K,V> extends HashMap<K,V> 
{
	protected V defaultValue;

	public DefaultHashMap(V defaultValue) 
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public V get(Object k) 
	{
		return containsKey(k) ? super.get(k) : defaultValue;
	}
}
