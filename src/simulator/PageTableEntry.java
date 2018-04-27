package simulator;

public class PageTableEntry {
    private int frameNumber;
    private boolean present;
    private boolean dirty;
    //TODO decide if you need this when implementing the page replacement algorithm
    private boolean referenced;

    public PageTableEntry(int frameNumber, boolean present, boolean dirty, boolean referenced) {
        this.frameNumber = frameNumber;
        this.present = present;
        this.dirty = dirty;
        this.referenced = referenced;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isReferenced() {
        return referenced;
    }

    public void setReferenced(boolean referenced) {
        this.referenced = referenced;
    }

    public String toString() {
        return "present: " + present + " dirty: " + dirty + " frame nr: " + frameNumber;
    }
}
