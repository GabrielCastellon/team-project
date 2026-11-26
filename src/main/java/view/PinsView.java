package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import interface_adapter.view_pins.ViewPinsPresenter;
import interface_adapter.view_pins.ViewPinsPresenter.PinCard;

/*View saved pins then display the current user's collection. End of Part 3 in sequence UML marks
selected photo saved. Opening pins - PinsView.showPins(...) puts their descriptions on screen.
Owner lookup - findByOwnerId(ownerId) matches pin to owner. Swing receives the pin description and
determines how to display them in List<String>. Separate responsibility from JSON storage or login.
 */
//inherits from JPanel
public class PinsView extends JPanel implements ViewPinsPresenter.View {
    //field for visible rows
    private final DefaultListModel<PinCard> pins = new DefaultListModel<>();
    private final JLabel status = new JLabel();

    public PinsView(){
        super(new BorderLayout());
        //add takes composition and position
        JList<PinCard> list = new JList<>(pins);
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();

        list.setCellRenderer((rows, item, index,
                              isSelected, hasFocus) -> {
            String caption = item.description() + "- Photo by " + item.photographer();
            return renderer.getListCellRendererComponent(rows, caption, index,
                    isSelected, hasFocus);
        });
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    @Override
    /*handles case (1) lookup found pin and description, and (2) success but no pins saved yet
    Presenter now owns conversion from use-case output to display data. PinsView depends on the
    presenter's PinCard, fixing dependency.
     */
    public void showPins(List<PinCard> items) {
        pins.clear();
        pins.addAll(items);
        if (items.isEmpty()) {
            status.setText("No saved pins yet");
        } else {
            status.setText("");
        }
    }

    @Override
    //handles case where lookup fails
    public void showError(String message){
        pins.clear();
        status.setText(message);
    }
}


