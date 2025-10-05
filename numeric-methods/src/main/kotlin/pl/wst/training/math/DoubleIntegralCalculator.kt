package pl.wst.training.math

import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Constant
import org.mariuszgromada.math.mxparser.Expression
import java.math.BigDecimal
import java.math.MathContext

/**
 * Calculates double integrals over simple domains using the trapezoidal rule (iterated integrals).
 *
 * Supported domains:
 * - Rectangle: x in [ax, bx], y in [ay, by] (Cartesian)
 * - Strip: x in [ax, bx], y in [yLow(x), yHigh(x)] where bounds are expressions in x
 * - Circle (disk): center (cx, cy), radius R (polar transform with Jacobian r)
 * - Ring (annulus): center (cx, cy), radii [rIn, rOut] (polar transform with Jacobian r)
 *
 * The public API mirrors the style of SingleIntegralCalculator to keep UI usage simple.
 */
class DoubleIntegralCalculator(
    private val stepsX: Int = DEFAULT_STEPS_X,
    private val stepsY: Int = DEFAULT_STEPS_Y,
    private val mathContext: MathContext = DEFAULT_MATH_CONTEXT
) {

    sealed interface Domain {
        data class Rectangle(val ax: BigDecimal, val bx: BigDecimal, val ay: BigDecimal, val by: BigDecimal): Domain
        data class Strip(val ax: BigDecimal, val bx: BigDecimal, val yLowExpr: String, val yHighExpr: String): Domain
        data class Circle(val cx: BigDecimal, val cy: BigDecimal, val r: BigDecimal): Domain
        data class Ring(val cx: BigDecimal, val cy: BigDecimal, val rIn: BigDecimal, val rOut: BigDecimal): Domain
    }

    private data class Mx2(val expr: Expression, val x: Argument, val y: Argument)

    /**
     * Integrate f(x,y) over domain D using trapezoidal rule with stepsX x stepsY grid.
     */
    fun integrate(expression: String, domain: Domain): BigDecimal {
        require(expression.isNotBlank()) { "expression must not be blank" }
        require(stepsX >= 1 && stepsY >= 1) { "stepsX and stepsY must be >= 1" }

        return when (domain) {
            is Domain.Rectangle -> integrateRectangle(expression, domain)
            is Domain.Strip -> integrateStrip(expression, domain)
            is Domain.Circle -> integrateCircle(expression, domain)
            is Domain.Ring -> integrateRing(expression, domain)
        }.round(mathContext)
    }

    // ---------------- Cartesian: Rectangle ----------------
    private fun integrateRectangle(expression: String, d: Domain.Rectangle): BigDecimal {
        val (ax, bx) = ordered(d.ax, d.bx)
        val (ay, by) = ordered(d.ay, d.by)
        if (ax == bx || ay == by) return BigDecimal.ZERO
        val f = build2(expression)
        val hx = stepSize(ax, bx, stepsX)
        val hy = stepSize(ay, by, stepsY)

        var sum = BigDecimal.ZERO
        for (ix in 0..stepsX) {
            val x = ax + hx * ix
            val wx = trapezoidWeight(ix, stepsX)
            for (iy in 0..stepsY) {
                val y = ay + hy * iy
                val wy = trapezoidWeight(iy, stepsY)
                val w = wx * wy
                val v = eval2(f, x, y)
                sum = sum.add(v.multiply(BigDecimal.valueOf(w), mathContext), mathContext)
            }
        }
        val area = hx.multiply(hy, mathContext)
        return area.multiply(sum, mathContext)
    }

    // ---------------- Cartesian: Strip y in [yLow(x), yHigh(x)] ----------------
    private fun integrateStrip(expression: String, d: Domain.Strip): BigDecimal {
        val (ax, bx) = ordered(d.ax, d.bx)
        if (ax == bx) return BigDecimal.ZERO

        // Build f(x,y)
        val f = build2(expression)
        // Build y bounds as functions of x
        val xArg = Argument("x", 0.0)
        val piConst = Constant("PI", Math.PI)
        val eConst = Constant("E", Math.E)
        val yLow = Expression(d.yLowExpr, xArg, piConst, eConst)
        val yHigh = Expression(d.yHighExpr, xArg, piConst, eConst)

        val hx = stepSize(ax, bx, stepsX)
        var outerSum = BigDecimal.ZERO
        for (ix in 0..stepsX) {
            val x = ax + hx * ix
            val wx = trapezoidWeight(ix, stepsX)
            xArg.argumentValue = x.toDouble()
            val y1 = BigDecimal.valueOf(yLow.calculate()).round(mathContext)
            val y2 = BigDecimal.valueOf(yHigh.calculate()).round(mathContext)
            if (y1.toDouble().isNaN() || y2.toDouble().isNaN()) {
                throw IllegalArgumentException("y-bound expressions evaluated to NaN at x=${x.toPlainString()}")
            }
            val (ay, by) = ordered(y1, y2)
            if (ay == by) continue
            val hy = stepSize(ay, by, stepsY)

            var innerSum = BigDecimal.ZERO
            for (iy in 0..stepsY) {
                val y = ay + hy * iy
                val wy = trapezoidWeight(iy, stepsY)
                val v = eval2(f, x, y)
                innerSum = innerSum.add(v.multiply(BigDecimal.valueOf(wy), mathContext), mathContext)
            }
            val inner = hy.multiply(innerSum, mathContext)
            outerSum = outerSum.add(inner.multiply(BigDecimal.valueOf(wx), mathContext), mathContext)
        }
        return hx.multiply(outerSum, mathContext)
    }

    // ---------------- Polar: Circle & Ring ----------------
    private fun integrateCircle(expression: String, d: Domain.Circle): BigDecimal {
        if (d.r.signum() <= 0) return BigDecimal.ZERO
        return integratePolar(expression, d.cx, d.cy, BigDecimal.ZERO, d.r)
    }

    private fun integrateRing(expression: String, d: Domain.Ring): BigDecimal {
        val (rIn, rOut) = ordered(d.rIn, d.rOut)
        if (rOut.signum() <= 0 || rIn == rOut) return BigDecimal.ZERO
        val r0 = if (rIn.signum() < 0) BigDecimal.ZERO else rIn
        return integratePolar(expression, d.cx, d.cy, r0, rOut)
    }

    private fun integratePolar(expression: String, cx: BigDecimal, cy: BigDecimal, rIn: BigDecimal, rOut: BigDecimal): BigDecimal {
        val f = build2(expression)
        val twoPi = BigDecimal.valueOf(2.0 * Math.PI)
        val hTheta = stepSize(BigDecimal.ZERO, twoPi, stepsX) // stepsX over theta
        val hR = stepSize(rIn, rOut, stepsY) // stepsY over radius

        var sum = BigDecimal.ZERO
        for (it in 0..stepsX) {
            val theta = BigDecimal.ZERO + hTheta * it
            val wt = trapezoidWeight(it, stepsX)
            for (ir in 0..stepsY) {
                val r = rIn + hR * ir
                val wr = trapezoidWeight(ir, stepsY)
                val x = cx + cos(theta).multiply(r, mathContext)
                val y = cy + sin(theta).multiply(r, mathContext)
                val v = eval2(f, x, y).multiply(r, mathContext)
                val w = wt * wr
                sum = sum.add(v.multiply(BigDecimal.valueOf(w), mathContext), mathContext)
            }
        }
        return hTheta.multiply(hR, mathContext).multiply(sum, mathContext)
    }

    // ---------------- Helpers ----------------
    private fun build2(expression: String): Mx2 {
        val x = Argument("x", 0.0)
        val y = Argument("y", 0.0)
        val piConst = Constant("PI", Math.PI)
        val eConst = Constant("E", Math.E)
        val expr = Expression(expression, x, y, piConst, eConst)
        return Mx2(expr, x, y)
    }

    private fun eval2(f: Mx2, x: BigDecimal, y: BigDecimal): BigDecimal {
        f.x.argumentValue = x.toDouble()
        f.y.argumentValue = y.toDouble()
        val v = f.expr.calculate()
        if (v.isNaN()) throw IllegalArgumentException("Expression evaluated to NaN at (x,y)=(${x.toPlainString()}, ${y.toPlainString()})")
        return BigDecimal.valueOf(v).round(mathContext)
    }

    private fun ordered(a: BigDecimal, b: BigDecimal): Pair<BigDecimal, BigDecimal> =
        if (a <= b) a to b else b to a

    private fun stepSize(a: BigDecimal, b: BigDecimal, steps: Int): BigDecimal =
        b.subtract(a, mathContext).divide(BigDecimal(steps), mathContext)

    private fun trapezoidWeight(i: Int, n: Int): Double = when (i) {
        0, n -> 0.5
        else -> 1.0
    }

    private operator fun BigDecimal.plus(other: BigDecimal) = this.add(other, mathContext)
    private operator fun BigDecimal.times(i: Int) = this.multiply(BigDecimal(i), mathContext)

    private fun sin(a: BigDecimal): BigDecimal = BigDecimal.valueOf(kotlin.math.sin(a.toDouble()))
    private fun cos(a: BigDecimal): BigDecimal = BigDecimal.valueOf(kotlin.math.cos(a.toDouble()))

    companion object {
        private const val DEFAULT_STEPS_X: Int = 500
        private const val DEFAULT_STEPS_Y: Int = 500
        private val DEFAULT_MATH_CONTEXT: MathContext = MathContext(50)
    }
}
