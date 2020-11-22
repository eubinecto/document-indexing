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

package uk.ac.man.cs.comp38211.util.pair;

public class PairOfObjectInt<L extends Comparable<L>> implements
        Comparable<PairOfObjectInt<L>>
{

    private L left;
    private int right;

    public PairOfObjectInt(L left, int right)
    {
        this.left = left;
        this.right = right;
    }

    public PairOfObjectInt()
    {
    }

    public L getLeftElement()
    {
        return left;
    }

    public int getRightElement()
    {
        return right;
    }

    public void set(L left, int right)
    {
        this.left = left;
        this.right = right;
    }

    public void setLeftElement(L left)
    {
        this.left = left;
    }

    public void setRightElement(int right)
    {
        this.right = right;
    }

    /**
     * Generates human-readable String representation of this pair.
     */
    public String toString()
    {
        return "(" + left + ", " + right + ")";
    }

    /**
     * Creates a shallow clone of this object; the left element itself is not
     * cloned.
     */
    public PairOfObjectInt<L> clone()
    {
        return new PairOfObjectInt<L>(left, right);
    }

    @Override
    public int compareTo(PairOfObjectInt<L> that)
    {
        if (this.left.equals(that.left))
        {
            return that.right > this.right ? -1 : 1;
        }
        return this.left.compareTo(that.left);
    }
}
