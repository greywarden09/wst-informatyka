package pl.wst.training.math

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.math.MathContext

private fun BigDecimal.shouldBeBetween(lower: BigDecimal, upper: BigDecimal) {
    ((this.compareTo(lower) >= 0) && (this.compareTo(upper) <= 0)) shouldBe true
}

class SingleIntegralCalculatorBehaviorSpec : BehaviorSpec({

    given("a calculator with default settings") {
        val calc = SingleIntegralCalculator()

        `when`("integrating a linear function x on [0,1]") {
            val result = calc.integrate("x", BigDecimal.ZERO, BigDecimal.ONE)
            then("the result should be approximately 0.5") {
                val lower = BigDecimal("0.4999")
                val upper = BigDecimal("0.5001")
                result.shouldBeBetween(lower, upper)
            }
        }

        `when`("integrating sin(x) on [0, PI]") {
            val result = calc.integrate("sin(x)", BigDecimal.ZERO, BigDecimal("3.14159265358979323846264338327950288419716939937510"))
            then("the result should be approximately 2") {
                val lower = BigDecimal("1.999")
                val upper = BigDecimal("2.001")
                result.shouldBeBetween(lower, upper)
            }
        }

        `when`("integrating with reversed bounds") {
            val forward = calc.integrate("x", BigDecimal.ZERO, BigDecimal.ONE)
            val backward = calc.integrate("x", BigDecimal.ONE, BigDecimal.ZERO)
            then("the result should be the negative of the forward integral") {
                backward shouldBe forward.negate()
            }
        }

        `when`("integrating over a zero-width interval") {
            val result = calc.integrate("x^2", BigDecimal("2"), BigDecimal("2"))
            then("the result should be zero") {
                result shouldBe BigDecimal.ZERO
            }
        }

        `when`("using a blank expression") {
            then("an IllegalArgumentException should be thrown") {
                shouldThrow<IllegalArgumentException> {
                    calc.integrate("   ", BigDecimal.ZERO, BigDecimal.ONE)
                }
            }
        }

        `when`("integrating an expression with a singularity inside the interval: 1/x on [-1,1]") {
            then("an IllegalArgumentException should be thrown due to singularity detection") {
                shouldThrow<IllegalArgumentException> {
                    calc.integrate("1/x", BigDecimal("-1"), BigDecimal("1"))
                }
            }
        }

        `when`("integrating an expression with a singularity at x=1 on [0,2]: 1/(x-1)") {
            then("an IllegalArgumentException should be thrown due to singularity detection") {
                shouldThrow<IllegalArgumentException> {
                    calc.integrate("1/(x-1)", BigDecimal("0"), BigDecimal("2"))
                }
            }
        }
    }

    given("a calculator configured with too few steps") {
        val calc = SingleIntegralCalculator(steps = 0, mathContext = MathContext(50))
        `when`("performing any integration") {
            then("an IllegalArgumentException should be thrown due to steps < 1") {
                shouldThrow<IllegalArgumentException> {
                    calc.integrate("x", BigDecimal.ZERO, BigDecimal.ONE)
                }
            }
        }
    }
})
