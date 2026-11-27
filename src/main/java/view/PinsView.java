package view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import interface_adapter.view_pins.ViewPinsPresenter;
import interface_adapter.view_pins.ViewPinsPresenter.PinCard;
import interface_adapter.discovery.DiscoveryPresenter.ThumbnailLoader;
import java.util.HashSet;
import java.util.Set;

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
    private final ThumbnailLoader thumbnailLoader;
    //maps icon to photo URL
    private Map<String, ImageIcon> thumbnails = new HashMap<>();
    private final SwingTaskRunner<ImageIcon> thumbnailTasks = new SwingTaskRunner<>();
    //checks if photo url is already downloading
    private final Set<String> loadingUrls = new HashSet<>();

    //Swing view for card for each pin
    public PinsView(ThumbnailLoader thumbnailLoader){
        super(new BorderLayout());
        this.thumbnailLoader = thumbnailLoader;
        //add takes composition and position
        JList<PinCard> list = new JList<>(pins);
        JPanel card = new JPanel(new BorderLayout(0,6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        JLabel thumbnail = new JLabel("Photo preview", SwingConstants.CENTER);
        thumbnail.setPreferredSize(new Dimension(240, 140));
        JLabel caption = new JLabel();
        JLabel credit = new JLabel();

        card.add(thumbnail, BorderLayout.NORTH);
        card.add(caption, BorderLayout.CENTER);
        card.add(credit, BorderLayout.SOUTH);

        list.setCellRenderer((rows, item, index, isSelected, hasFocus) -> {
            String url = item.imageURL();
            ImageIcon icon = thumbnails.get(url);
            thumbnail.setIcon(icon); //Swing reuses this label for every row

            if(icon != null) {
                thumbnail.setText("");
            } else if (thumbnails.containsKey(url)) {
                thumbnail.setText("Preview unavailable");
            } else {
                thumbnail.setText("Loading photo...");
            }
            caption.setText(item.description());
            credit.setText("Photo by "+ item.photographer());

            Color background = rows.getBackground();
            Color foreground = rows.getForeground();
            if(isSelected) {
                background = rows.getSelectionBackground();
                foreground = rows.getSelectionForeground();
            }
            card.setBackground(background);
            thumbnail.setForeground(foreground);
            caption.setForeground(foreground);
            credit.setForeground(foreground);
            return card;
        });
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    private ImageIcon loadThumbnail(String imageURL) throws Exception {
        BufferedImage photo = thumbnailLoader.load(imageURL);

        //scales real image into size of thumbnail
        double scale = Math.min(240.0/photo.getWidth(), 140.0/photo.getHeight());
        int width = Math.max(1, (int) (photo.getWidth()*scale));
        int height = Math.max(1, (int) (photo.getHeight()*scale));

        Image preview = photo.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(preview);
    }
    @Override
    /*handles case (1) lookup found pin and description, and (2) success but no pins saved yet
    Presenter now owns conversion from use-case output to display data. PinsView depends on the
    presenter's PinCard, fixing dependency.
     */
    public void showPins(List<PinCard> items) {
        Map<String, ImageIcon> currentThumbnails = new HashMap<>();
        thumbnails = currentThumbnails;

        pins.clear();
        pins.addAll(items);


        for (PinCard item: items){
            String url = item.imageURL();
            //skip a download that is already running; handles duplicate download
            if(loadingUrls.contains(url)){
                continue;
            }
            loadingUrls.add(url); //records loading photo
            thumbnailTasks.run(
                    () -> loadThumbnail(url), //SwingTaskRunner downloads and scales photo
                    (icon, error) -> {
                        loadingUrls.remove(url); //removes url once attempt is finished
                        //checks if card currently on screen still needs this url
                        for (int row = 0; row<pins.size(); row++){
                            PinCard card = pins.getElementAt(row);
                            if(url.equals(card.imageURL())){
                                thumbnails.put(url,icon);//maps url to icon when job finished
                                //asks Swing to draw the card again rendering the thumbnail
                                repaint();
                                break;
                            }
                        }

                    }
            );
        }
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


