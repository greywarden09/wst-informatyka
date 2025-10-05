package pl.wst.training

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import org.mariuszgromada.math.mxparser.License
import pl.wst.training.ui.GuiFactory
import pl.wst.training.ui.MainMenuWindow

/**
 * Bootstraps and runs the Numerical Calculator application.
 * Keep this class minimal; UI is assembled in dedicated window classes.
 */
class Application {
    /**
     * Starts the application.
     *
     * Responsibilities:
     * - Confirms non-commercial use of the math library (mXparser)
     * - Creates and starts the Lanterna screen and GUI
     * - Opens the main menu window and blocks until it is closed
     * - Ensures the screen is stopped afterward
     */
    fun start() {
        License.iConfirmNonCommercialUse("Marcin Las")
        val (screen, gui) = GuiFactory.createScreenAndGui()
        try {
            openMainWindow(gui)
        } finally {
            screen.stopScreen()
        }
    }

    private fun openMainWindow(gui: MultiWindowTextGUI) {
        val mainWindow = MainMenuWindow(gui)
        gui.addWindowAndWait(mainWindow)
    }
}
