package org.transitclock.core.barefoot;

import java.util.Objects;

/**
 * Encapsulate stopPathIndex and a segmentIndex of a TTC Indices.
 */
public class ReferenceId {
    private int stopPathIndex;
    private int segmentIndex;

    public ReferenceId(int stopPathIndex, int segmentIndex) {
        this.stopPathIndex = stopPathIndex;
        this.segmentIndex = segmentIndex;
    }

    public int getStopPathIndex() {
        return stopPathIndex;
    }
    public void setStopPathIndex(int stopPathIndex) {
        this.stopPathIndex = stopPathIndex;
    }
    public int getSegmentIndex() {
        return segmentIndex;
    }
    public void setSegmentIndex(int segmentIndex) {
        this.segmentIndex = segmentIndex;
    }
    public static ReferenceId deconstructRefId(long refId)
    {
        // decode two ints from a long
        // https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
        int stopPathIndex = (int)(refId >> 32);
        int segmentIndex = (int)refId;
        return new ReferenceId(stopPathIndex, segmentIndex);
    }

    public long getRefId()
    {
        // encode two ints into a long
        // https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
        return  (((long)segmentIndex) << 32) | (stopPathIndex & 0xffffffffL);
    }

    @Override
    public String toString() {
        return "ReferenceId [stopPathIndex=" + stopPathIndex + ", segmentIndex=" + segmentIndex + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(segmentIndex, stopPathIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReferenceId other = (ReferenceId) obj;
        return segmentIndex == other.segmentIndex && stopPathIndex == other.stopPathIndex;
    }

}