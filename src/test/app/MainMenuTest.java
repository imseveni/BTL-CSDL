package app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MainMenuTest {

    @Test
    void menuConsoleConLaChinModule() {
        assertEquals(9, new MainMenu().getModules().size());
    }
}
