package simulator;

public class VirtualAddress {

    private int virtualPageNumber;
    private int offset;

    public  VirtualAddress(int virtualPageNumber, int offset) {
        this.virtualPageNumber = virtualPageNumber;
        this.offset = offset;
    }

    public int getVirtualPageNumber() {
        return virtualPageNumber;
    }

    public void setVirtualPageNumber(int virtualPageNumber) {
        this.virtualPageNumber = virtualPageNumber;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String toString(){
        return "Virtual page number: " + virtualPageNumber + " offset: " + offset;
    }
}
