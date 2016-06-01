/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package OpenBitSet;

import java.util.BitSet;
/**
 *
 * @author PC
 */
public class LongBitSet {
    private int nBitSets;
    private int BitSetSize;
    private BitSet[] bitSets;
    private long numberOfAddedElements;
    public LongBitSet(long numberOfBits){        
        this.BitSetSize = Integer.MAX_VALUE;
        this.nBitSets = (int)(numberOfBits/(long)this.BitSetSize);
        this.nBitSets++;
        bitSets = new BitSet[nBitSets];
        for (int i=0;i<this.nBitSets;i++){
            bitSets[i] = new BitSet(this.BitSetSize);
        }
        this.numberOfAddedElements = 0;
    }
    public boolean get(long index){
        int row, col;
        row = (int) (index/(long)this.BitSetSize);
        col = (int) (index%(long)this.BitSetSize);
        return bitSets[row].get(col);
    }
    public void set(long index){
        if (this.get(index)==false){
            this.numberOfAddedElements++;
        }
        int row, col;
        row = (int) (index/(long)this.BitSetSize);
        col = (int) (index%(long)this.BitSetSize);
        bitSets[row].set(col, true);
    }
}
