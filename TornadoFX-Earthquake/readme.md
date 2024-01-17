# Project Earthquake

## Use and Features
- start application in src/Initial/main/kotlin/EarthQuakeGUI.kt

- With the Application is possible to get Earthquake data with specific parameters
- the Application allows to show the data in a userfriendly form
- the Label above the GUI elements is helpful to get feedback while the application is working on the asynchronous calls
- further is possible to set a filter by place, to get a better overview over specific regions 
  - reset filtering by place: in the dropdown there is an empty cell (at the bottom), activate the filter on this cell shows all places again.
  - the filter range is rendered of the current available data from the api
- another possible feather is to show a specific time range:
  - a data can be entered as "year-MM-dd" for example "2023-12-01" and then the time range is set until the current day.
  - a single button allows to access the current day 
- is possible to combine both filters together to get a clearer picture

- additionly there is a feature which shows a bar chart over the shown data which extracts the maximum magnitude of the day - like this is possiple the get a clear overview where strong eruptions happen. 
- the number of events per place or time range can be looked with the index of the current event in the table - this option in the table is an alternative instead another api call (Code Commented out because it was delivered)
- at least there is a function which exports the current data to a csv which is saved "src/Inital/main/resources/output_earthquake.csv"

## Limits
- The API call starts from the "2023-11-22" as the application was set up initially like this with the delivered code 

