package pl.wst.training.ui

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object DialogUtil {
    /**
     * Displays a simple information dialog with a single OK button.
     * Intended for short, user-friendly messages.
     *
     * @param gui Active Lanterna GUI to attach the dialog to
     * @param message Message body; long messages are wrapped for readability
     * @param title Optional dialog title (defaults to "Information")
     */
    fun showInfo(gui: MultiWindowTextGUI, message: String, title: String = "Information") {
        val dialog = MessageDialogBuilder()
            .setTitle(title)
            .setText(wrapText(message))
            .addButton(MessageDialogButton.OK)
            .build()
        dialog.showDialog(gui)
    }

    /**
     * Displays a warning dialog (non-fatal) with a single OK button.
     * Use this for validation errors or recoverable issues.
     *
     * @param gui Active Lanterna GUI to attach the dialog to
     * @param message Warning text; long messages are wrapped for readability
     * @param title Optional dialog title (defaults to "Warning")
     */
    fun showWarning(gui: MultiWindowTextGUI, message: String, title: String = "Warning") {
        val dialog = MessageDialogBuilder()
            .setTitle(title)
            .setText(wrapText(message))
            .addButton(MessageDialogButton.OK)
            .build()
        dialog.showDialog(gui)
    }

    /**
     * Shows a result dialog with an extra button to copy only the result to the clipboard.
     * @param gui GUI instance
     * @param message Full message shown to the user (wrapped for readability)
     * @param result Raw result string to be copied when user presses the copy button
     * @param title Dialog title (defaults to "Result")
     */
    fun showResult(gui: MultiWindowTextGUI, message: String, result: String, title: String = "Result") {
        val window = BasicWindow(title).apply {
            setHints(mutableListOf(Window.Hint.CENTERED))
        }

        val root = Panel(GridLayout(1))
        root.addComponent(EmptySpace(TerminalSize(0, 1)))
        root.addComponent(Label(wrapText(message)))
        root.addComponent(EmptySpace(TerminalSize(0, 1)))

        val buttons = Panel(GridLayout(2))
        buttons.addComponent(Button("Copy result") {
            copyToClipboard(result)
        })
        val okButton = Button("OK") {
            window.close()
        }
        buttons.addComponent(okButton)
        root.addComponent(buttons)

        window.component = root
        window.focusedInteractable = okButton
        gui.addWindowAndWait(window)
    }

    private fun copyToClipboard(text: String) = try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Throwable) {
    }

    private fun wrapText(message: String, lineLength: Int = 76): String {
        if (message.length <= lineLength) return message
        val words = message.split(Regex("\\s+"))
        val sb = StringBuilder()
        var currentLen = 0
        for (word in words) {
            val addLen = if (currentLen == 0) word.length else word.length + 1 // include space
            if (currentLen + addLen > lineLength) {
                sb.append('\n')
                sb.append(word)
                currentLen = word.length
            } else {
                if (currentLen > 0) sb.append(' ')
                sb.append(word)
                currentLen += addLen
            }
        }
        return sb.toString()
    }
}
