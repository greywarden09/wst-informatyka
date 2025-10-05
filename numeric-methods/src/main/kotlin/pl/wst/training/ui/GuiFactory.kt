package pl.wst.training.ui

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame

object GuiFactory {
    /**
     * Creates and starts a Lanterna TerminalScreen together with a MultiWindowTextGUI.
     *
     * Side effects:
     * - Starts the screen (you must call screen.stopScreen() when done)
     * - Centers Swing-based terminal, if used
     *
     * @return Pair of (TerminalScreen, MultiWindowTextGUI)
     */
    fun createScreenAndGui(): Pair<TerminalScreen, MultiWindowTextGUI> {
        val terminal = DefaultTerminalFactory()
            .setInitialTerminalSize(TerminalSize(80, 24))
            .setTerminalEmulatorTitle("Numerical Calculator")
            .createTerminal()

        if (terminal is SwingTerminalFrame) {
            terminal.setLocationRelativeTo(null)
        }

        val screen = TerminalScreen(terminal)
        screen.startScreen()
        val gui = MultiWindowTextGUI(screen)
        return screen to gui
    }
}
