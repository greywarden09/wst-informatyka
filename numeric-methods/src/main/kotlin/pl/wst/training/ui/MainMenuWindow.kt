package pl.wst.training.ui

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Window

/**
 * Main menu window of the Numerical Calculator.
 * Offers navigation to available calculators and the Exit action.
 * This is intentionally simple for educational purposes.
 */
class MainMenuWindow(gui: MultiWindowTextGUI) : BasicWindow("Numerical Calculator") {

    init {
        setHints(mutableListOf(Window.Hint.CENTERED))

        component = Panel(GridLayout(1)).apply {
            preferredSize = TerminalSize(40, 5)

            addComponent(EmptySpace(TerminalSize(0, 1)))
            addComponent(Label("=== Numerical Calculator ===").apply {
                layoutData = GridLayout.createLayoutData(
                    GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false
                )
            })
            addComponent(EmptySpace(TerminalSize(0, 1)))

            addComponent(Button("Single integral") { gui.addWindowAndWait(SingleIntegralWindow(gui)) })
            addComponent(Button("Double integral") { DialogUtil.showInfo(gui, "Double integral selected") })
            addComponent(Button("Numerical derivative") { DialogUtil.showInfo(gui, "Numerical derivative selected") })

            addComponent(EmptySpace(TerminalSize(0, 1)))
            addComponent(Button("Exit") { this@MainMenuWindow.close() })
        }
    }
}
