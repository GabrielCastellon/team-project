package interface_adapter.view_pins;

import java.util.ArrayList;
import java.util.List;
import use_case.view_pins.ViewPinsOutputBoundary;
import use_case.view_pins.ViewPinItem;

// outputboundary is the interface called by the interactor
public class ViewPinsPresenter implements ViewPinsOutputBoundary {

    public record PinCard(String description, String imageURL, String photographer) {

    }
    //view interface describes two methods that the presenter calls; keeps swing out of interactor
    public interface View {
        void showPins(List<PinCard> cards); //presenter calls on the view
        void showError(String message);
    }

    public final View view;

    public ViewPinsPresenter(View view){
        this.view = view;
    }

    //interactor calls this
    @Override
    public void presentPins(List<ViewPinItem> items){
        List<PinCard> cards = new ArrayList<>();

        //presenter turns one saved item into one card
        for (ViewPinItem item: items){
            PinCard card = new PinCard(
                    item.description(),
                    item.imageURL(),
                    item.photographer()
            );
            cards.add(card);
        }

        /*view receives the whole collection; if empty, view receives empty list and shows no
        saved pins yet
         */
        view.showPins(cards);
    }

    @Override
    public void presentError(String message){
        view.showError(message);
    }
}


