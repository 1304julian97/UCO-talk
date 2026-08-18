package co.edu.uco.xebia.bank.shared

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

type Rate = Rate.T
object Rate extends RefinedSubtype[BigDecimal, Positive]
