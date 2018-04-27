package simulator;

public class PhysicalAddress {

    private int frameNumber;
    private int offset;

    public PhysicalAddress(int frameNumber, int offset) {
        this.frameNumber = frameNumber;
        this.offset = offset;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String toString() {
        return "Frame number: " + frameNumber + " offset: " + offset;
    }
}
