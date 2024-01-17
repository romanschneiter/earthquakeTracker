
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import tornadofx.*
import java.text.SimpleDateFormat
import kotlin.concurrent.fixedRateTimer

/**
 * Initialize GUI
 */
class TableApp : App(EarthQuakeTableView::class) {
    override fun start(stage: Stage) {
        stage.width = 1100.0
        stage.height = 700.0
        super.start(stage)
    }
}

/**
 * creating GUI Elements
 */
class EarthQuakeTableView : View("Earthquake Tracker") {

    //get initial data
    private var myEarthQuakeData = getEarthQuakes()
    private var formatedUserfriedly = formatUserfriedly(myEarthQuakeData).asObservable()
    private val getSetOfPlaces = getSetOfPlaces(myEarthQuakeData)
    private val selectedPlace = SimpleObjectProperty<String>()
    private var quantity: TextField by singleAssign()
    private var status: Label by singleAssign()
    private var filterValuePlace = ""
    private var filterValueTime = "yyyy-MM-dd"
    private var barChart = createBarChart(formatedUserfriedly)
    private val dateValidationMessageProperty = SimpleStringProperty("Date must be after 2023-11-22")
    private val placesWithNullOption = FXCollections.observableArrayList(getSetOfPlaces + null)

    //set event listener
    init {
        selectedPlace.addListener { _, _, _ ->
        }
    }

    //prepare GUI elements for filtering and display data
    override val root = borderpane {
        center {
            vbox(alignment = Pos.CENTER_LEFT, spacing = 10) {

                //Status as overview - helpful while loading new data
                hbox(spacing = 10.0, alignment = Pos.CENTER_LEFT) {
                    status = label {
                        text = "Status: Choose any option"
                        style { fontWeight = FontWeight.BOLD }
                    }
                }

                //GUI elements for filtering place
                hbox(spacing = 10.0, alignment = Pos.CENTER_LEFT) {
                    label("Filter Place:\n ")
                    combobox(selectedPlace, values = placesWithNullOption)

                    button("Filter Place") {
                        prefWidth = 150.0
                        action {
                            try {
                                filterValuePlace = selectedPlace.value?: "Unselected"

                                //Filter by Value is set or not?
                                if (filterValuePlace == "Unselected") {
                                    status.text = "Status: All works normal."
                                    status.textFill = c(0, 0, 0) // Color.BLACK
                                } else {
                                    status.text = "Status: Filtered by $filterValuePlace."
                                    status.textFill = c(0, 0, 0) // Color.BLACK
                                }

                            } catch (e: Exception) {
                                status.text = "Attention: ${e.message}."
                                status.textFill = c(255, 0, 0) // Color.RED
                            }
                        }
                    }
                }

                //GUI elements for filtering time range
                hbox(spacing = 10.0, alignment = Pos.CENTER_LEFT) {
                    label("Filter time range(year-MM-dd) from desired date until today :\n ")
                    quantity = textfield("yyyy-MM-dd") {
                        prefWidth = 150.0
                        filterInput { it.controlNewText.isNotEmpty() }
                    }

                    val dateValidationLabel = label(dateValidationMessageProperty) {
                        style {
                            fontSize = 10.px
                            textFill = Color.BLACK
                        }
                    }

                    button("filter time range") {
                        prefWidth = 150.0
                        action {
                            try {
                                //Set Status
                                if(!isValidDateFormat(quantity.text)){
                                    dateValidationMessageProperty.set("Date was not accepted, must be after 2023-11-22")
                                    quantity.text = "yyyy-MM-dd"
                                    dateValidationLabel.style {
                                        fontSize = 10.px
                                        textFill = Color.RED
                                    }
                                }else {
                                    dateValidationMessageProperty.set("Date must be after 2023-11-22")
                                    dateValidationLabel.style {
                                        fontSize = 10.px
                                        textFill = Color.BLACK
                                    }
                                }

                                //When date is valid and in time range save value in variable otherwise default
                                val inputDate = SimpleDateFormat("yyyy-MM-dd").parse(quantity.text)
                                val requiredDate = SimpleDateFormat("yyyy-MM-dd").parse("2023-11-22")
                                if (inputDate.after(requiredDate) && isValidDateFormat(quantity.text)) {
                                    filterValueTime = quantity.text
                                status.text = "Status: All works normal."
                                status.textFill = Color.BLACK
                                    dateValidationMessageProperty.set("Date is ok.")
                                    dateValidationLabel.style {
                                        fontSize = 10.px
                                        textFill = Color.GREEN
                                    }
                                } else {
                                    // Set the date back to the default value if it's not valid
                                    quantity.text = "yyyy-MM-dd"

                                    status.text = "Status: Date was not accepted."
                                    status.textFill = Color.RED

                                    // Update the validation message
                                    dateValidationMessageProperty.set("Date was not accepted, must be after 2023-11-22")
                                    dateValidationLabel.style {
                                        fontSize = 10.px
                                        textFill = Color.RED
                                    }
                                }
                                } catch (e: Exception) {
                                status.text = "Attention: ${e.message}."
                                status.textFill = Color.RED
                            }
                        }
                    }

                    //GUI element for filter data by current day
                    button("Values of Today") {
                        prefWidth = 150.0
                        action {
                            try {
                                filterValueTime = getCurrentDate()
                                status.text = "Status: All works normal."
                                status.textFill = Color.BLACK
                            } catch (e: Exception) {
                                status.text = "Attention: ${e.message}."
                                status.textFill = Color.RED
                            }
                        }
                    }
                }

                //Asynchronous call to get current Data with responsability which filter is currently activated
                fixedRateTimer("Timer", true, 0L, 10000) {
                    //lateinit var data: ChuckJokes
                    runAsync {
                        try {
                            // filtering Place and time range
                            if (filterValuePlace.isNotEmpty() && filterValueTime != "yyyy-MM-dd") {
                                val filterStart = filterValueTime // Update this with your desired start date
                                val filteredDataRange = filterEarthquakesByDateRange(filterStart)

                                if(filterValuePlace === "Unselected"){
                                    formatedUserfriedly.setAll(filteredDataRange)
                                }else{
                                    //Intersection of filter by place and time range
                                    val filteredData = filteredDataRange.filter {
                                        it.place?.contains(filterValuePlace, ignoreCase = true) ?: false
                                    }
                                    formatedUserfriedly.setAll(filteredData)
                                }

                            } else if (filterValuePlace.isNotEmpty()) {
                                val filteredData = filterEarthquakesByPlace(filterValuePlace)
                                formatedUserfriedly.setAll(filteredData)

                            // filtering by time range
                            } else if (filterValueTime != "yyyy-MM-dd") {
                                val filterStart = filterValueTime // Update this with your desired start date
                                val filteredData = filterEarthquakesByDateRange(filterStart)
                                formatedUserfriedly.setAll(filteredData)

                            // no filter set
                            } else {
                                myEarthQuakeData = getEarthQuakes()
                                formatedUserfriedly.setAll(formatUserfriedly(myEarthQuakeData))
                            }
                        } catch (e: Exception) {
                            throw e
                        }
                    } ui { _ ->
                        barChart.data = generateData(formatedUserfriedly)
                        add(barChart)

                    } fail { e ->
                        println("Error: $e")
                    }
                }

                //place data in table overview
                tableview(formatedUserfriedly) {
                    readonlyColumn("Index", Earthquake::id)
                    readonlyColumn("Magnitude", Earthquake::magnitude)
                    readonlyColumn("Place", Earthquake::place)
                    readonlyColumn("Time", Earthquake::time)
                    readonlyColumn("Type", Earthquake::type)
                    readonlyColumn("Titel", Earthquake::title)

                    // No empty columns
                    columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY

                    // Table fit parent
                    vboxConstraints {
                        vGrow = Priority.NEVER
                    }
                }

                //GUI element to export and save current data in CSV
                hbox(spacing = 10.0, alignment = Pos.CENTER_LEFT) {

                    button("export current values to CSV") {
                        prefWidth = 200.0
                        action {
                            try {
                                val outputCsvFilePath =
                                    "src/Inital/main/resources/output_earthquake.csv" // Replace with your output file path
                                writeWeatherDataToCSV(outputCsvFilePath, formatedUserfriedly)
                                status.text = "Status: All works normal."
                                status.textFill = Color.BLACK
                            } catch (e: Exception) {
                                status.text = "Attention: ${e.message}."
                                status.textFill = Color.RED
                            }
                        }
                    }
                }
            }
        }
    }
}

//launch application
fun main(args: Array<String>) {
    launch<TableApp>(args)
}