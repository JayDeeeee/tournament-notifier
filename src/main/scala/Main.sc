import java.io.File

import com.github.tototoshi.csv._

val writer = CSVWriter.open("eventsOld.csv")
val cwd = new File(".").getAbsolutePath


writer.writeRow(Vector("a", "b"))

writer.close()