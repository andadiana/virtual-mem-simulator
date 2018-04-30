package presentation.data;

public class MemoryItem {

    private Integer frameNumber;
    private Integer address;
    private Integer data;

    public MemoryItem(int frameNumber, int address, int data) {
        this.frameNumber = frameNumber;
        this.address = address;
        this.data = data;
    }

    public Integer getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(Integer frameNumber) {
        this.frameNumber = frameNumber;
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
        this.data = data;
    }
}
