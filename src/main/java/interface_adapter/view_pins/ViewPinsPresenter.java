package interface_adapter.view_pins;

import java.util.List;
import use_case.view_pins.ViewPinsOutputBoundary;

// outputboundary is the interface called by the interactor
public class ViewPinsPresenter implements ViewPinsOutputBoundary {

    //view interface describes two methods that the presenter calls; keeps swing out of interactor
    public interface View {
        void showPins(List<String> descriptions);
        void showError(String message);
    }

    public final View view;

    public ViewPinsPresenter(View view){
        this.view = view;
    }

    @Override
    public void presentPins(List<String> descriptions){
        view.showPins(descriptions);
    }

    @Override
    public void presentError(String message){
        view.showError(message);
    }
}


