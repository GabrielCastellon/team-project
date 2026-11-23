package interface_adapter.save_pin;

import java.util.ArrayList;
import java.util.List;

//Part 3: stores  current pin state and listeners asking about change before Discover shows 'saved'
public class SavePinViewModel {
    private SavePinState state;
    private final List<SavePinViewModelListener> listeners = new ArrayList<>();

    //View (discover) reads viewModel.getState() for planned screen behavior
    public SavePinState getState(){
        return state;
    }

    public void addListener(SavePinViewModelListener listener){
        listeners.add(listener);
    }

    //stores new pin state and calls stateChanged() on each listener (observer pattern)
    //subject is view model and observer is view object
    public void setState(SavePinState state){
        this.state = state; //replaces view model's previous snapshot with presenter supplied state
        for (SavePinViewModelListener listener: listeners){
            listener.stateChanged(); //refers to view implementation; viewmodel only needs interface
        }
    }
}
