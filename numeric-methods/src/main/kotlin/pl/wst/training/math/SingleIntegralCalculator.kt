package pl.wst.training.math

import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Constant
import org.mariuszgromada.math.mxparser.Expression
import java.math.BigDecimal
import java.math.MathContext

/**
 * Calculates a definite single integral using the trapezoidal rule.
 *
 * - Supports variables: x, and predefined constants: pi/PI and e/E
 * - Uses BigDecimal arithmetic with a configurable MathContext
 * - Performs a lightweight scan to detect potential singularities in the interval
 *
 * The public API is kept stable for UI code.
 */
class SingleIntegralCalculator(
    private val steps: Int = DEFAULT_STEPS,
    private val mathContext: MathContext = DEFAULT_MATH_CONTEXT
) {
    private data class MxExpr(val expr: Expression, val xArg: Argument)

    /**
     * Integrates the given expression over [lowerBound, upperBound].
     *
     * @param expression an expression in terms of x, compatible with mXparser (radian trig)
     * @param lowerBound lower bound (inclusive)
     * @param upperBound upper bound (inclusive)
     * @throws IllegalArgumentException on invalid input, singularities, or evaluation errors
     */
    fun integrate(expression: String, lowerBound: BigDecimal, upperBound: BigDecimal): BigDecimal {
        require(steps >= 1) { "steps must be >= 1" }
        require(expression.isNotBlank()) { "expression must not be blank" }

        if (lowerBound == upperBound) return BigDecimal.ZERO

        val (a, b, sign) = if (lowerBound < upperBound) {
            Triple(lowerBound, upperBound, BigDecimal.ONE)
        } else {
            Triple(upperBound, lowerBound, BigDecimal.ONE.negate())
        }

        val expr = buildExpression(expression)
        val h = stepSize(a, b)

        checkForSingularity(expr, a, b, h)

        // Trapezoidal rule: h * [ (f(a)+f(b))/2 + sum_{i=1..n-1} f(a + i*h) ]
        val fa = evaluateAt(expr, a)
        val fb = evaluateAt(expr, b)

        var sum = fa.add(fb, mathContext).multiply(HALF, mathContext)
        for (i in 1 until steps) {
            val xi = a.add(h.multiply(BigDecimal(i), mathContext), mathContext)
            sum = sum.add(evaluateAt(expr, xi), mathContext)
        }
        return sign.multiply(h, mathContext).multiply(sum, mathContext)
    }

    private fun buildExpression(expression: String): MxExpr {
        // mXparser uses radians for trigonometric functions by default.
        // Provide uppercase constants PI and E for compatibility with previous behavior.
        val xArg = Argument("x", 0.0)
        val piConst = Constant("PI", Math.PI)
        val eConst = Constant("E", Math.E)
        val expr = Expression(expression, xArg, piConst, eConst)
        return MxExpr(expr, xArg)
    }

    private fun stepSize(a: BigDecimal, b: BigDecimal): BigDecimal =
        b.subtract(a, mathContext).divide(BigDecimal(steps), mathContext)

    private fun evaluateAt(expr: MxExpr, x: BigDecimal): BigDecimal = try {
        expr.xArg.argumentValue = x.toDouble()
        val v = expr.expr.calculate()
        if (v.isNaN()) throw IllegalArgumentException("Expression evaluated to NaN at x=${x.toPlainString()}")
        BigDecimal.valueOf(v).round(mathContext)
    } catch (e: Exception) {
        throw IllegalArgumentException(
            "Invalid expression or evaluation error at x=${x.toPlainString()}: ${e.message}", e
        )
    }

    private fun checkForSingularity(expr: MxExpr, a: BigDecimal, b: BigDecimal, h: BigDecimal) {
        val eps = h.divide(BigDecimal.TEN, mathContext)
        for (i in 0..steps) {
            val xi = a.add(h.multiply(BigDecimal(i), mathContext), mathContext)
            val valueOrNull = try { evaluateAt(expr, xi) } catch (_: Exception) { null }

            val looksBad = when {
                valueOrNull == null -> true
                isTooLarge(valueOrNull) -> true
                else -> false
            }
            if (looksBad && isProblematicAround(expr, xi, a, b, eps)) {
                throw IllegalArgumentException(
                    "Detected possible singularity near x=${xi.toPlainString()}. The integral over [${a.toPlainString()}, ${b.toPlainString()}] may require Cauchy's principal value and is not supported by this method."
                )
            }
        }
    }

    private fun isTooLarge(v: BigDecimal): Boolean = v.abs() > SINGULARITY_THRESHOLD

    private fun isProblematicAround(
        expr: MxExpr,
        x: BigDecimal,
        a: BigDecimal,
        b: BigDecimal,
        eps: BigDecimal
    ): Boolean {
        val left = x.subtract(eps, mathContext)
        if (left >= a) {
            val fl = try { evaluateAt(expr, left) } catch (_: Exception) { null }
            if (fl == null || isTooLarge(fl)) return true
        }
        val right = x.add(eps, mathContext)
        if (right <= b) {
            val fr = try { evaluateAt(expr, right) } catch (_: Exception) { null }
            if (fr == null || isTooLarge(fr)) return true
        }
        return false
    }

    companion object {
        private const val DEFAULT_STEPS: Int = 10_000
        private val DEFAULT_MATH_CONTEXT: MathContext = MathContext(50)
        private val HALF = BigDecimal("0.5")
        private val PI = BigDecimal("3.14159265358979323846264338327950288419716939937510")
        private val E = BigDecimal("2.71828182845904523536028747135266249775724709369995")

        private val SINGULARITY_THRESHOLD = BigDecimal("1E40")
    }
}
