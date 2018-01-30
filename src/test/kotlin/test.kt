fun main(args: Array<String>) {
  val a = 0xff
  println(a.toString(16))
  val b: Byte = -1
  println((b.toInt() and 0xff).toString(16))
}