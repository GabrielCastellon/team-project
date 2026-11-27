package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import interface_adapter.view_pins.ViewPinsPresenter;

/*View saved pins then display the current user's collection. End of Part 3 in sequence UML marks
selected photo saved. Opening pins - PinsView.showPins(...) puts their descriptions on screen.
Owner lookup - findByOwnerId(ownerId) matches pin to owner. Swing receives the pin description and
determines how to display them in List<String>. Separate responsibility from JSON storage or login.
 */
//inherits from JPanel
public class PinsView extends JPanel implements ViewPinsPresenter.View {
    //field for visible rows
    private final DefaultListModel<String> descriptions = new DefaultListModel<>();
    private final JLabel status = new JLabel();

    public PinsView(){
        super(new BorderLayout());
        //add takes composition and position
        add(new JScrollPane(new JList<>(descriptions)), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    @Override
    //handles case (1) lookup found pin and description, and (2) success but no pins saved yet
    public void showPins(List<String> photoDescriptions){
        descriptions.clear();
        for (String description: photoDescriptions){
            descriptions.addElement(description); //append to swing list model
        }
        if (photoDescriptions.isEmpty()) {
            status.setText("No saved pins yet");
        } else {
            status.setText("");
        }
    }

    @Override
    //handles case where lookup fails
    public void showError(String message){
        descriptions.clear();
        status.setText(message);
    }
}


