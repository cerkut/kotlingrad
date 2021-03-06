package edu.umontreal.kotlingrad.samples

import edu.umontreal.kotlingrad.experimental.*
import edu.umontreal.kotlingrad.utils.step
import guru.nidi.graphviz.engine.Format.DOT
import java.io.File
import kotlin.math.absoluteValue

fun main() {
  for (i in 0..9) {
    val bt = ExpressionGenerator.scaledRandomBiTree(4)
    plotOracle("oracle$i.svg", 1.0) { bt(it).toDouble() }
    File("btree_rand$i.tex").writeText(bt.render(DOT).toString().lines().drop(1)
      .joinToString("\n").let { "\\digraph[scale=0.2]{btree$i} {\n$it" })
    ((-1.0..1.0) step 0.01).joinToString("\n") { "$it, ${bt(it)}" }.saveAs("btree_rand$i.csv")
  }
}

object ExpressionGenerator: Protocol<DReal>(DReal) {
  val sum = { left: SFun<DReal>, right: SFun<DReal> -> left + right }
  val mul = { left: SFun<DReal>, right: SFun<DReal> -> left * right }

  val operators = listOf(sum, mul)
  override val variables = listOf(x)

  infix fun SFun<DReal>.wildOp(that: SFun<DReal>) = operators.random(rand)(this, that)

  fun randomBiTree(height: Int = 5): SFun<DReal> =
    if (height == 0) (listOf(wrap(rand.nextDouble(-1.0, 1.0))) + variables).random(rand)
    else randomBiTree(height - 1) wildOp randomBiTree(height - 1)

  fun scaledRandomBiTree(height: Int = 4, maxX: Double = 1.0, maxY: Double = 1.0) =
    randomBiTree(height).let { it - it(0.0) }.let {
      it * wrap(maxY) / ((-maxX..maxX) step 0.01).toList()
        .map { num -> it(num).toDouble().absoluteValue }.max()!!
    }
}

fun plotOracle(filename: String, maxX: Double = 1.0, oracle: (Double) -> Double) {
  val t = ((-maxX..maxX) step 0.01).toList()
  mapOf("x" to t, "y" to t.map { oracle(it) }).plot2D("Oracle", filename)
}