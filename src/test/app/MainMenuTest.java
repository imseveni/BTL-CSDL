package app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MainMenuTest {

    @Test
    void menuHas10Modules() {
        assertEquals(10, new MainMenu().getModules().size());
    }
}
