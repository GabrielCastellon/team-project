package use_case.save_pin;

//interface gives controller way to submit request to interactor
public interface SavePinInputBoundary {
    void execute(SavePinInputData inputData);
}

