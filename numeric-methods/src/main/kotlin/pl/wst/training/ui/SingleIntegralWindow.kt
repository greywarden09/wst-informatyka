package pl.wst.training.ui

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.Window
import pl.wst.training.math.SingleIntegralCalculator
import java.math.BigDecimal

/**
 * Interactive window for computing a single definite integral using the trapezoidal rule.
 *
 * Users provide:
 * - f(x) expression (EvalEx syntax, supports variables x and constants e, pi)
 * - Lower and upper bounds
 * - Optional number of steps (defaults to 10000)
 *
 * The result is displayed in a dialog with a copy-to-clipboard shortcut.
 */
class SingleIntegralWindow(private val gui: MultiWindowTextGUI) : BasicWindow(TITLE) {

    companion object {
        private const val TITLE = "Single integral"
        private const val HEADER = "=== Single Integral ==="
        private const val LABEL_EQUATION = "f(x)="
        private const val LABEL_LOWER = "lower range"
        private const val LABEL_UPPER = "upper range"
        private const val LABEL_STEPS = "steps"
        private val SIZE_EQ_FIELD = TerminalSize(30, 1)
        private val SIZE_RANGE_FIELD = TerminalSize(10, 1)
        private val FILL = GridLayout.createLayoutData(
            GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true
        )
        private val CENTER_BOTTOM = GridLayout.createLayoutData(
            GridLayout.Alignment.CENTER, GridLayout.Alignment.END, false, false
        )
        private val CENTER = GridLayout.createLayoutData(
            GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false
        )
    }

    private val equationField = TextBox().apply { preferredSize = SIZE_EQ_FIELD }
    private val lowerField = TextBox().apply { preferredSize = SIZE_RANGE_FIELD }
    private val upperField = TextBox().apply { preferredSize = SIZE_RANGE_FIELD }
    private val stepsField = TextBox().apply {
        preferredSize = SIZE_RANGE_FIELD
        text = "10000"
    }

    init {
        component = buildContent()
        setHints(mutableListOf(Window.Hint.CENTERED))
    }

    private fun buildContent(): Panel {
        val root = Panel(GridLayout(1)).apply {
            addComponent(spacer())
            addComponent(titleLabel())
            addComponent(spacer())
            addComponent(buildForm())
            addComponent(filler())
            addComponent(centeredButton("Calculate") { onCalculateClicked() })
        }.also {
            it.addComponent(centeredButton("Exit") { this.close() })
        }
        return root
    }

    private fun buildForm(): Panel = Panel(GridLayout(2)).apply {
        addComponent(Label(LABEL_EQUATION))
        addComponent(equationField)
        addComponent(Label(LABEL_LOWER))
        addComponent(lowerField)
        addComponent(Label(LABEL_UPPER))
        addComponent(upperField)
        addComponent(Label(LABEL_STEPS))
        addComponent(stepsField)
    }

    private fun onCalculateClicked() {
        val expr = equationField.text.trim()
        val lowerText = lowerField.text.trim()
        val upperText = upperField.text.trim()
        val stepsText = stepsField.text.trim()

        if (expr.isEmpty()) {
            DialogUtil.showInfo(gui, "Please enter an expression for f(x).", "Error")
            return
        }

        val lower = parseBigDecimal(lowerText)
        val upper = parseBigDecimal(upperText)
        if (lower == null || upper == null) {
            DialogUtil.showInfo(gui, "Lower and upper ranges must be valid numbers.", "Error")
            return
        }

        val stepsValueOrNull = if (stepsText.isEmpty()) null else stepsText.toIntOrNull()
        if (stepsText.isNotEmpty() && stepsValueOrNull == null) {
            DialogUtil.showInfo(gui, "Steps must be a positive integer.", "Error")
            return
        }
        if (stepsValueOrNull != null && stepsValueOrNull < 1) {
            DialogUtil.showInfo(gui, "Steps must be >= 1.", "Error")
            return
        }

        try {
            val calculator = if (stepsValueOrNull != null) SingleIntegralCalculator(stepsValueOrNull) else SingleIntegralCalculator()
            val result = calculator.integrate(expr, lower, upper)
            val resultText = result.toPlainString()
            DialogUtil.showResult(
                gui,
                "Integral of f(x)=$expr on [${lower.toPlainString()}, ${upper.toPlainString()}] = $resultText",
                resultText,
                title = "Result"
            )
        } catch (e: IllegalArgumentException) {
            DialogUtil.showWarning(gui, e.message ?: "Calculation error")
        } catch (e: Exception) {
            DialogUtil.showWarning(gui, "Unexpected error: ${e.message}")
        }
    }

    private fun parseBigDecimal(text: String): BigDecimal? = try {
        BigDecimal(text)
    } catch (_: Exception) {
        null
    }

    private fun centeredButton(label: String, onClick: () -> Unit) =
        Button(label) { onClick() }.apply { layoutData = CENTER_BOTTOM }

    private fun titleLabel(): Label = Label(HEADER).apply { layoutData = CENTER }

    private fun spacer() = EmptySpace(TerminalSize(0, 1))

    private fun filler() = EmptySpace(TerminalSize(0, 0)).apply { layoutData = FILL }
}
