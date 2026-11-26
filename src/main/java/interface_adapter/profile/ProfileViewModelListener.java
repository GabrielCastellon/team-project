package interface_adapter.profile;

//observer interface: the view implements it so the view model never depends on the view class
/** Notified whenever the profile view model gets a new state. */
public interface ProfileViewModelListener {
    /** Called after {@link ProfileViewModel#setState} replaces the state. */
    void stateChanged();
}
