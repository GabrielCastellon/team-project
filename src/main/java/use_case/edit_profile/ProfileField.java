package use_case.edit_profile;

//names the profile field a validation failure is about, so the view can show the error next to it
/** Identifies which profile field a validation failure belongs to. */
public enum ProfileField {
    DISPLAY_NAME,
    BIOGRAPHY,
    AVATAR
}
