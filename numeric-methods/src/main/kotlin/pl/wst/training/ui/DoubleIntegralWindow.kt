package pl.wst.training.ui

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import pl.wst.training.math.DoubleIntegralCalculator
import java.math.BigDecimal

/**
 * Interactive window for computing a double integral over selected domain.
 *
 * f(x,y) supports variables x and y and constants PI, E (mXparser syntax; trig in radians).
 */
class DoubleIntegralWindow(private val gui: MultiWindowTextGUI) : BasicWindow(TITLE) {

    companion object {
        private const val TITLE = "Double integral"
        private const val HEADER = "=== Double Integral ==="
        private val SIZE_EQ_FIELD = TerminalSize(34, 1)
        private val SIZE_SMALL = TerminalSize(12, 1)
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

    private val exprField = TextBox().apply { preferredSize = SIZE_EQ_FIELD }
    private val stepsXField = TextBox("400").apply { preferredSize = SIZE_SMALL }
    private val stepsYField = TextBox("400").apply { preferredSize = SIZE_SMALL }

    private val domainSelector = ComboBox<String>("Rectangle", "Strip", "Circle (disk)", "Ring (annulus)")
    private val domainPanel = Panel(GridLayout(2))

    // Rectangle inputs
    private val rectAx = TextBox().apply { preferredSize = SIZE_SMALL }
    private val rectBx = TextBox().apply { preferredSize = SIZE_SMALL }
    private val rectAy = TextBox().apply { preferredSize = SIZE_SMALL }
    private val rectBy = TextBox().apply { preferredSize = SIZE_SMALL }

    // Strip inputs (y bounds as expressions of x)
    private val stripAx = TextBox().apply { preferredSize = SIZE_SMALL }
    private val stripBx = TextBox().apply { preferredSize = SIZE_SMALL }
    private val stripYLow = TextBox().apply { preferredSize = SIZE_EQ_FIELD }
    private val stripYHigh = TextBox().apply { preferredSize = SIZE_EQ_FIELD }

    // Circle inputs
    private val circleCx = TextBox().apply { preferredSize = SIZE_SMALL }
    private val circleCy = TextBox().apply { preferredSize = SIZE_SMALL }
    private val circleR = TextBox().apply { preferredSize = SIZE_SMALL }

    // Ring inputs
    private val ringCx = TextBox().apply { preferredSize = SIZE_SMALL }
    private val ringCy = TextBox().apply { preferredSize = SIZE_SMALL }
    private val ringRin = TextBox().apply { preferredSize = SIZE_SMALL }
    private val ringRout = TextBox().apply { preferredSize = SIZE_SMALL }

    init {
        component = buildContent()
        setHints(mutableListOf(Window.Hint.CENTERED))
        domainSelector.addListener { _, _, _ -> rebuildDomainPanel() }
        rebuildDomainPanel()
    }

    private fun buildContent(): Panel {
        val root = Panel(GridLayout(1)).apply {
            addComponent(spacer())
            addComponent(Label(HEADER).apply { layoutData = CENTER })
            addComponent(spacer())
            addComponent(buildForm())
            addComponent(filler())
            addComponent(centeredButton("Calculate") { onCalculateClicked() })
            addComponent(centeredButton("Exit") { this@DoubleIntegralWindow.close() })
        }
        return root
    }

    private fun buildForm(): Panel = Panel(GridLayout(2)).apply {
        addComponent(Label("f(x,y)="))
        addComponent(exprField)

        addComponent(Label("steps X"))
        addComponent(stepsXField)
        addComponent(Label("steps Y"))
        addComponent(stepsYField)

        addComponent(Label("Domain"))
        addComponent(domainSelector)

        addComponent(EmptySpace(TerminalSize(0,1)))
        addComponent(domainPanel)
    }

    private fun rebuildDomainPanel() {
        domainPanel.removeAllComponents()
        when (domainSelector.selectedItem) {
            "Rectangle" -> buildRectanglePanel()
            "Strip" -> buildStripPanel()
            "Circle (disk)" -> buildCirclePanel()
            else -> buildRingPanel()
        }
        domainPanel.invalidate()
    }

    private fun buildRectanglePanel() {
        with(domainPanel) {
            addComponent(Label("x from"))
            addComponent(rectAx)
            addComponent(Label("x to"))
            addComponent(rectBx)
            addComponent(Label("y from"))
            addComponent(rectAy)
            addComponent(Label("y to"))
            addComponent(rectBy)
        }
    }

    private fun buildStripPanel() {
        with(domainPanel) {
            addComponent(Label("x from"))
            addComponent(stripAx)
            addComponent(Label("x to"))
            addComponent(stripBx)
            addComponent(Label("y_low(x)="))
            addComponent(stripYLow)
            addComponent(Label("y_high(x)="))
            addComponent(stripYHigh)
            addComponent(EmptySpace(TerminalSize(0,1)))
            addComponent(Label("Tip: y-bounds are expressions using x; use PI, E; trig in radians."))
        }
    }

    private fun buildCirclePanel() {
        with(domainPanel) {
            addComponent(Label("center cx"))
            addComponent(circleCx)
            addComponent(Label("center cy"))
            addComponent(circleCy)
            addComponent(Label("radius R"))
            addComponent(circleR)
        }
    }

    private fun buildRingPanel() {
        with(domainPanel) {
            addComponent(Label("center cx"))
            addComponent(ringCx)
            addComponent(Label("center cy"))
            addComponent(ringCy)
            addComponent(Label("r inner"))
            addComponent(ringRin)
            addComponent(Label("r outer"))
            addComponent(ringRout)
        }
    }

    private fun onCalculateClicked() {
        val expr = exprField.text.trim()
        if (expr.isEmpty()) {
            DialogUtil.showInfo(gui, "Please enter an expression for f(x,y).", "Error")
            return
        }
        val stepsX = parseInt(stepsXField.text.trim())
        val stepsY = parseInt(stepsYField.text.trim())
        if (stepsX == null || stepsX < 1 || stepsY == null || stepsY < 1) {
            DialogUtil.showInfo(gui, "Steps must be positive integers.", "Error")
            return
        }

        val calculator = DoubleIntegralCalculator(stepsX, stepsY)
        try {
            val domain = buildDomain() ?: return
            val result = calculator.integrate(expr, domain)
            val msg = when (domain) {
                is DoubleIntegralCalculator.Domain.Rectangle -> "∬_Rect f(x,y) dxdy = ${result.toPlainString()}"
                is DoubleIntegralCalculator.Domain.Strip -> "∬_Strip f(x,y) dxdy = ${result.toPlainString()}"
                is DoubleIntegralCalculator.Domain.Circle -> "∬_Disk f(x,y) dxdy = ${result.toPlainString()}"
                is DoubleIntegralCalculator.Domain.Ring -> "∬_Ring f(x,y) dxdy = ${result.toPlainString()}"
            }
            DialogUtil.showResult(gui, msg, result.toPlainString(), title = "Result")
        } catch (e: IllegalArgumentException) {
            DialogUtil.showWarning(gui, e.message ?: "Calculation error")
        } catch (e: Exception) {
            DialogUtil.showWarning(gui, "Unexpected error: ${e.message}")
        }
    }

    private fun buildDomain(): DoubleIntegralCalculator.Domain? {
        return when (domainSelector.selectedItem) {
            "Rectangle" -> {
                val ax = parseBD(rectAx.text.trim())
                val bx = parseBD(rectBx.text.trim())
                val ay = parseBD(rectAy.text.trim())
                val by = parseBD(rectBy.text.trim())
                if (ax == null || bx == null || ay == null || by == null) {
                    DialogUtil.showInfo(gui, "Please provide valid numbers for rectangle bounds.", "Error")
                    null
                } else DoubleIntegralCalculator.Domain.Rectangle(ax, bx, ay, by)
            }
            "Strip" -> {
                val ax = parseBD(stripAx.text.trim())
                val bx = parseBD(stripBx.text.trim())
                val yLow = stripYLow.text.trim()
                val yHigh = stripYHigh.text.trim()
                if (ax == null || bx == null || yLow.isEmpty() || yHigh.isEmpty()) {
                    DialogUtil.showInfo(gui, "Provide x-range and both y-bound expressions.", "Error")
                    null
                } else DoubleIntegralCalculator.Domain.Strip(ax, bx, yLow, yHigh)
            }
            "Circle (disk)" -> {
                val cx = parseBD(circleCx.text.trim())
                val cy = parseBD(circleCy.text.trim())
                val r = parseBD(circleR.text.trim())
                if (cx == null || cy == null || r == null) {
                    DialogUtil.showInfo(gui, "Provide center (cx,cy) and radius R.", "Error")
                    null
                } else DoubleIntegralCalculator.Domain.Circle(cx, cy, r)
            }
            else -> {
                val cx = parseBD(ringCx.text.trim())
                val cy = parseBD(ringCy.text.trim())
                val rIn = parseBD(ringRin.text.trim())
                val rOut = parseBD(ringRout.text.trim())
                if (cx == null || cy == null || rIn == null || rOut == null) {
                    DialogUtil.showInfo(gui, "Provide center and both radii.", "Error")
                    null
                } else DoubleIntegralCalculator.Domain.Ring(cx, cy, rIn, rOut)
            }
        }
    }

    private fun parseBD(text: String): BigDecimal? = try { BigDecimal(text) } catch (_: Exception) { null }
    private fun parseInt(text: String): Int? = text.toIntOrNull()

    private fun centeredButton(label: String, onClick: () -> Unit) =
        Button(label) { onClick() }.apply { layoutData = CENTER_BOTTOM }

    private fun spacer() = EmptySpace(TerminalSize(0, 1))
    private fun filler() = EmptySpace(TerminalSize(0, 0)).apply { layoutData = FILL }
}
