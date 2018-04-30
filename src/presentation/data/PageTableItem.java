package presentation.data;

public class PageTableItem {

    private int virtualPageNumber;
    private int frameNumber;
    private boolean present;
    private boolean dirty;

    public PageTableItem(int virtualPageNumber, int frameNumber, boolean present, boolean dirty) {
        this.virtualPageNumber = virtualPageNumber;
        this.frameNumber = frameNumber;
        this.present = present;
        this.dirty = dirty;
    }
    public int getVirtualPageNumber() {
        return virtualPageNumber;
    }

    public void setVirtualPageNumber(int virtualPageNumber) {
        this.virtualPageNumber = virtualPageNumber;
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
}
