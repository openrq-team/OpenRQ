package net.fec.openrq;


import java.util.Iterator;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface SourceBlockStream extends Iterable<SourceBlock> {

    /**
     * {@inheritDoc}
     * 
     * @exception IllegalStateException
     *                If the iterator has already been returned
     */
    @Override
    public Iterator<SourceBlock> iterator();
}
