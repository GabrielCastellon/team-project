package use_case.view_pins;

/*
ViewPinsController --> ViewPinsInputBoundary; similar to part 1 sequence UML. Controller depends
on input boundary which is implemented by interactor. Viewing collection needs only ownerId because
request is showing all pins associated with the owner.
 */
public interface ViewPinsInputBoundary {
    void execute(String ownerId);
}
