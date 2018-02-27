package pubg.radar

import org.apache.commons.io.IOUtils
import org.apache.commons.math3.stat.regression.SimpleRegression

fun main(args: Array<String>) {
    val x_LR = SimpleRegression()
    val y_LR = SimpleRegression()
//  val func = { x: Double -> 2 * x - 1 }
//  for (x in 1..100)
//    simpleRegression.addData(x.toDouble(), func(x.toDouble()))
//  println("slope=${simpleRegression.slope}")
//  println("intercept=${simpleRegression.intercept}")
//  println("func(-1.0)=${simpleRegression.predict(-1.0)}")
    val content = IOUtils.readLines(Thread.currentThread().contextClassLoader.getResourceAsStream("location.txt"), "UTF-8")
    content.forEach {
        val line = it.split(";")
        x_LR.addData(line[0].toDouble(), line[2].toDouble())
        y_LR.addData(line[1].toDouble(), line[3].toDouble())
    }
    println("slope=${x_LR.slope}")
    println("intercept=${x_LR.intercept}")
    println("R=${x_LR.r}")
    println("slope=${y_LR.slope}")
    println("intercept=${y_LR.intercept}")
    println("R=${y_LR.r}")
//  println(content.size)
}
