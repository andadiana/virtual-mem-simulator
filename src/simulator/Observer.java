package simulator;

import java.util.List;
import java.util.Queue;

public interface Observer {

    public void update(Queue<OperationStep> operationSteps);
}
