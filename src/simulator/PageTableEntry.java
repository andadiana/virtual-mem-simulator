package simulator;

public class PageTableEntry {
    private int frameNumber;
    private boolean present;
    private boolean dirty;

    public PageTableEntry(int frameNumber, boolean present, boolean dirty) {
        this.frameNumber = frameNumber;
        this.present = present;
        this.dirty = dirty;
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

    public String toString() {
        return "present: " + present + " dirty: " + dirty + " frame nr: " + frameNumber;
    }
}
