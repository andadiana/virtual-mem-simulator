package simulator;

public class OperationStep {

    private OperationType type;
    private int address;

    public OperationStep (OperationType type, int address) {
        this.type = type;
        this.address = address;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String toString() {
        return type + ": " + address;
    }
}
