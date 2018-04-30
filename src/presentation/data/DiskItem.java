package presentation.data;

public class DiskItem {

    private Integer virtualPageNumber;
    private Integer address;
    private Integer data;

    public DiskItem(int virtualPageNumber, int address, int data) {
        this.virtualPageNumber = virtualPageNumber;
        this.address = address;
        this.data = data;
    }

    public Integer getVirtualPageNumber() {
        return virtualPageNumber;
    }

    public void setVirtualPageNumber(Integer virtualPageNumber) {
        this.virtualPageNumber = virtualPageNumber;
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
