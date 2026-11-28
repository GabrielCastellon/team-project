package interface_adapter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class ProfileViewModelTest {

    @Test
    void startsEmpty() {
        ProfileViewModel viewModel = new ProfileViewModel();

        assertEquals("", viewModel.getState().getDisplayName());
        assertEquals("", viewModel.getState().getStatusMessage());
    }

    @Test
    void setStateStoresStateAndNotifiesEveryListener() {
        ProfileViewModel viewModel = new ProfileViewModel();
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        viewModel.addListener(first::incrementAndGet);
        viewModel.addListener(second::incrementAndGet);
        ProfileState state = new ProfileState("Kieran", "", "", "", "", "", "", false, true);

        viewModel.setState(state);

        assertSame(state, viewModel.getState());
        assertEquals(1, first.get());
        assertEquals(1, second.get());
    }
}
