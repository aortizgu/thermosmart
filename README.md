# thermosmart

## Design Document
---
### Main Objetive

The app will manage thermostats by setting the temperature threshold and notifiing to the user when the heater is activated or deactivated. In Addition will show the weather of the thermostat location.

---
### Implementations:
- Has 6 screens for navigation
  - ThermostatListFragment
  - AppConfigFragment
  - ThermostatSaveFragment
  - ThermostatDetailFragment
  - ThermostatConfigFragment
  - SelectLocationFragment
- login to the app with your user via email or google account using firebase authentication.
- has a firebase real time database to manage thermostat configurations.
- Use android shared preferences interface to configure your units preferences ºC / ºF.
- The user managed thermostats will be shown in a ReciclerView.
- ConstraintLayouts are mostly used in all layouts.
- Thermostat location will be selected using google maps sdk.
- Map will use the device location to move the camera there when no marker selected.
- Will be notified when a thermostat heater is activated/deactivated.
- Thermostat loaction weather temperature will be shown using openweathermap api.
- Thermostat loaction weather image will be shown using openweathermap api.
- Navigation has animations

---
### Notes
- to simulate a thermostat temperature change, a similator is provided for the device "devicetestuuid1" https://thermosmart-b5382.web.app
- firebase real time database [datamodel](design/datamodel.png)

---
### Milestones
- v1: design document and dummy app (21/12/2021).
- v2: be able to add and remove thermostats to the managed thermostats list using the firestore realtime database (22/12/2021).
- v3: add notifications and fetch external data using openweathermap api (23/12/2021).
- v4: add animations and style (24/12/2021).