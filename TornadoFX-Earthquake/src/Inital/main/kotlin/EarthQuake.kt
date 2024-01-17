/** Programming with Kotlin,
 *  Computer Science, Bern University of Applied Sciences */

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

// current date, and end of filter in format "yyyy-MM-dd"
val currentDate: LocalDate = LocalDate.now()
val todayBySystemtime : String = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
const val today = "2023-11-22"
val filterEnd = todayBySystemtime

// Query all available events with parameters
const val urlQuery =
   "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=$today&updatedafter=${today}T14:00:00"

/**
 * Properties of an earthquake events
 *
 * @property mag - Magnitude
 * @property place - place
 * @property time
 * @property type
 * @property title
 * @constructor Create empty Properties
 *
 * You can add more properties when needed
 */
@Serializable
data class Properties(
   val mag: Double?,
   val place: String?,
   val time: Long,
   val type: String,
   val title: String
)


/**
 * Feature - describes one earthquake event
 *
 * @property type = "Feature"
 * @property properties
 * @constructor Create empty Feature
 */
@Serializable
data class Feature(
   val type: String,
   val properties: Properties
)


/**
 * Feature collection - Collection of earthquake events
 *
 * @property type = "FeatureCollection"
 * @property features - Array of earthquake events
 * @constructor Create empty Feature collection
 */
@Serializable
data class FeatureCollection(
   val type: String,
   val features: ArrayList<Feature>
)


/**
 * Used to get Data from the API
 * @return FeatureCollection from API with earthquake parameters for UI
 */
fun getEarthQuakes(): FeatureCollection {
   val jsonString = URI(urlQuery).toURL().readText()
   val json = Json { ignoreUnknownKeys = true }
   return json.decodeFromString<FeatureCollection>(jsonString)
}


/**
 * Returns current data to show events of today
 * @return current data
 */
fun getCurrentDate(): String {
   return filterEnd
}


/**
 * formats the date representation in a human friedly readable form
 * @param timestamp - Data as long representation
 * @return formated timestamp as readable value
 */
fun formatTimestamp(timestamp: Long): String {
   val instant = Instant.ofEpochMilli(timestamp)
   val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneId.systemDefault()) // You can change this to your desired time zone
   return formatter.format(instant)
}


/**
 * current data class for a single Earthquake event - to use the data in accessible format
 */
data class Earthquake(
   val id: Int = 0,
   val magnitude: Double? = 0.0,
   val place: String? = "Unknown",
   val time: String = "Unknown",
   val type: String = "Unknown",
   val title: String = "Unknown",
)


/**
 * prepares inputs for the combobox to filter by a specific place
 * @param dataRawFromApi - Feature Collection to filter all Places for the current Selector
 * @return distinct list of places to use in the combobox
 */
   fun getSetOfPlaces(dataRawFromApi: FeatureCollection): Set<String>{
      val distinctSetPlaces: Set<String> = dataRawFromApi.features.map {
         it.properties.place?.let { place ->
            val regex = Regex(",\\s*(.+)") // Define the regex pattern
            val matchResult = regex.find(place)
            matchResult?.groupValues?.get(1) ?: "Unknown"
         } ?: "Unknown"
      }.toSet()
      return distinctSetPlaces
   }


/**
 * formats FeatureCollection to a List of Earthquake Objects - to process and show in the UI
 * @param dataRawFromApi - Feature Collection which was filtered by Place or TimeRange
 * @return List of Earthquake Objects to process in UI
 */
   fun formatUserfriedly(dataRawFromApi: FeatureCollection): List<Earthquake> {
      return dataRawFromApi.features.mapIndexed { index, feature ->
         Earthquake(
            id = index + 1,
            magnitude = feature.properties.mag,
            place = feature.properties.place,
            time = formatTimestamp(feature.properties.time),
            type = feature.properties.type,
            title = feature.properties.title,
         )
      }
   }


/**
 * filter Inputs from API by a specific place
 * (Combination of filtering time range and place is combined in UI Class)
 * @param filteredPlace - desired filter
 * @return filtered List by place
 */
   fun filterEarthquakesByPlace(filteredPlace : String): List<Earthquake> {
      val myEarthQuakeData = getEarthQuakes()

      return myEarthQuakeData.features.filter { feature ->
         val place = feature.properties.place ?: "Unknown"
         // Use contains or equals, depending on your filtering needs
         place.contains(filteredPlace, ignoreCase = true)
      }.mapIndexed { index, feature ->
         Earthquake(
            id = index + 1,
            magnitude = feature.properties.mag,
            place = feature.properties.place ?: "Unknown",
            time = formatTimestamp(feature.properties.time),
            type = feature.properties.type,
            title = feature.properties.title,
         )
      }
   }


/**
 * filter Inputs from API by a specific time range
 * (Combination of filtering time range and place is combined in UI Class)
 * @param filterStart - desired starting point for the time range (endpoint of the time range is the current day)
 * @return filtered List by specific time range
 */
   fun filterEarthquakesByDateRange(filterStart : String): List<Earthquake> {
      val myEarthQuakeData = getEarthQuakes()

      return myEarthQuakeData.features
         .filter { feature ->
            val eventTimestamp = feature.properties.time
            val eventDate = formatTimestamp(eventTimestamp).substringBefore(" ")

            // Check if the eventDate is within the specified range
            eventDate in filterStart..filterEnd
         }
         .mapIndexed { index, feature ->
            Earthquake(
               id = index + 1,
               magnitude = feature.properties.mag,
               place = feature.properties.place,
               time = formatTimestamp(feature.properties.time),
               type = feature.properties.type,
               title = feature.properties.title,
            )
         }
   }

/**
 * write current data to a local csv file
 * @param earthquakeDataList - current data to write in the csv
 * @param outputFilePath - path to save the data to csv
 */
fun writeWeatherDataToCSV(outputFilePath: String, earthquakeDataList: List<Earthquake>) {
    try {
       val csvFile = File(outputFilePath)
       csvFile.bufferedWriter().use { writer ->

          writer.write("Index,Magnitude,Place,Time,Type,Titel, Timestamp\n")

          // Write data
          for (earthquakeData in earthquakeDataList) {
             writer.write(
                "${earthquakeData.id},${earthquakeData.magnitude}," +
                        "${earthquakeData.place},${earthquakeData.time}," +
                        "${earthquakeData.type},${earthquakeData.title}\n"
             )
          }
       }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * create an instance from BarChart
 * @param earthquakeDataList - the data which will be processed and shown in the current BarChart
 * @return barChart - finished barChart with the distinct data per day
 */
fun createBarChart(earthquakeDataList: List<Earthquake>): BarChart<String, Number> {
   val xAxis = CategoryAxis()
   val yAxis = NumberAxis()
   val barChart = BarChart(xAxis, yAxis)
   barChart.title = "Max Magnitude per Day"

   // Assuming you have a function to get earthquake data
   val data = generateData(earthquakeDataList)
   barChart.data = data

   return barChart
}

/**
 * places the distinct data into the barchart (X and Y axis)
 * @param earthquakeList - the data to process and show in the barChart
 * @return FXCollections.observableArrayList(series) - return an observableArrayList to show in the Barchart
 */
 fun generateData(earthquakeList: List<Earthquake>): ObservableList<XYChart.Series<String, Number>> {
   val groupedData = groupDataByDay(earthquakeList)
   val series = XYChart.Series<String, Number>()

   for ((date, maxMagnitude) in groupedData) {
      val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(date)
      val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date)
      val xAxisLabel = "$month $dayOfMonth"
      series.data.add(XYChart.Data(xAxisLabel, maxMagnitude))
   }
   return FXCollections.observableArrayList(series)
}

/**
 * create the distinct data per day and extracts the max Magnitude per Day
 * @param earthquakeList - the data which gets distinct by Day and the maximum magnitude extracted
 * @return groupedData - the distinct data by day
 */
private fun groupDataByDay(earthquakeList: List<Earthquake>): Map<Date, Double> {
   val groupedData = mutableMapOf<Date, Double>()

   for (earthquake in earthquakeList) {
      val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(earthquake.time)
      val currentMaxMagnitude = groupedData[date]

      if (currentMaxMagnitude == null || (earthquake.magnitude ?: 0.0) > currentMaxMagnitude) {
         groupedData[date] = earthquake.magnitude ?: 0.0
      }
   }
   return groupedData
}

/**
 * checks if the data has a valid format
 * @param date - string to check
 */
 fun isValidDateFormat(date: String): Boolean {
   return try {
      SimpleDateFormat("yyyy-MM-dd").apply { isLenient = false }.parse(date)
      true
   } catch (e: ParseException) {
      false
   }
}
