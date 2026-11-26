package use_case.view_profile;

/** Input data for the view-profile use case: whose profile to load. */
public class ViewProfileInputData {
    private final String userId;

    public ViewProfileInputData(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
