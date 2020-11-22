/*
 * Cloud9: A MapReduce Library for Hadoop Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package uk.ac.man.cs.comp38211.io.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import uk.ac.man.cs.comp38211.util.map.HMapKF;
import uk.ac.man.cs.comp38211.util.map.MapKF;

/**
 * Writable representing a map from keys of arbitrary WritableComparable to
 * floats.
 * 
 * @param <K>
 *            type of key
 * 
 * @author Jimmy Lin
 */
public class HMapKFW<K extends WritableComparable<?>> extends HMapKF<K>
        implements Writable
{
    private static final long serialVersionUID = 1343885977770942281L;

    /**
     * Creates a <code>HMapKFW</code> object.
     */
    public HMapKFW()
    {
        super();
    }

    /**
     * Deserializes the map.
     * 
     * @param in
     *            source for raw byte representation
     */
    @Override
    @SuppressWarnings("unchecked")
    public void readFields(DataInput in) throws IOException
    {
        this.clear();

        int numEntries = in.readInt();
        if (numEntries == 0) return;

        String keyClassName = in.readUTF();

        K objK;
        try
        {
            Class<K> keyClass = (Class<K>) Class.forName(keyClassName);
            for (int i = 0; i < numEntries; i++)
            {
                objK = (K) keyClass.newInstance();
                objK.readFields(in);
                float s = in.readFloat();
                put(objK, s);
            }
        }
        catch (Exception e)
        {
            throw new IOException("Unable to create HMapKFW!");
        }
    }

    /**
     * Serializes the map.
     * 
     * @param out
     *            where to write the raw byte representation
     */
    public void write(DataOutput out) throws IOException
    {
        // Write out the number of entries in the map.
        out.writeInt(size());
        if (size() == 0) return;

        // Write out the class names for keys and values assuming that all keys
        // have the same type.
        Set<MapKF.Entry<K>> entries = entrySet();
        MapKF.Entry<K> first = entries.iterator().next();
        K objK = first.getKey();
        out.writeUTF(objK.getClass().getCanonicalName());

        // Then write out each key/value pair.
        for (MapKF.Entry<K> e : entrySet())
        {
            e.getKey().write(out);
            out.writeFloat(e.getValue());
        }
    }

    /**
     * Returns the serialized representation of this object as a byte array.
     * 
     * @return byte array representing the serialized representation of this
     *         object
     * @throws IOException
     */
    public byte[] serialize() throws IOException
    {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(bytesOut);
        write(dataOut);

        return bytesOut.toByteArray();
    }

    /**
     * Creates a <code>HMapKFW</code> object from a <code>DataInput</code>.
     * 
     * @param in
     *            source for reading the serialized representation
     * @return a newly-created <code>HMapKFW</code> object
     * @throws IOException
     */
    public static <T extends WritableComparable<?>> HMapKFW<T> create(
            DataInput in) throws IOException
    {
        HMapKFW<T> m = new HMapKFW<T>();
        m.readFields(in);

        return m;
    }

    /**
     * Creates a <code>HMapKFW</code> object from a byte array.
     * 
     * @param bytes
     *            source for reading the serialized representation
     * @return a newly-created <code>HMapKFW</code> object
     * @throws IOException
     */
    public static <T extends WritableComparable<?>> HMapKFW<T> create(
            byte[] bytes) throws IOException
    {
        return create(new DataInputStream(new ByteArrayInputStream(bytes)));
    }
}
