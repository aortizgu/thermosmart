#if defined(ESP32)
#include <WiFi.h>
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>
#define WIFI_SSID ""
#define WIFI_PASSWORD ""
#define API_KEY ""
#define DATABASE_URL ""
#define USER_EMAIL ""
#define USER_PASSWORD ""

FirebaseAuth auth;
FirebaseConfig config;
FirebaseData fbdo;

const int periodMs = 60000;
OneWire oneWireObj(D5);
DallasTemperature sensorDS18B20(&oneWireObj);

void setup() {
   Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("setup: Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("setup: Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  Serial.printf("setup: Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);
  config.api_key = API_KEY;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.database_url = DATABASE_URL;
  config.token_status_callback = tokenStatusCallback;  //see addons/TokenHelper.h
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  sensorDS18B20.begin();
}

void loop() {
  sensorDS18B20.requestTemperatures();
  Serial.print("temperature: ");
  const float temp = sensorDS18B20.getTempCByIndex(0);
  Serial.print(temp);
  Serial.println(" ºC");

  if (Firebase.ready()) {
    digitalWrite(LED_BUILTIN, LOW);
    delay(200);
    digitalWrite(LED_BUILTIN, HIGH);
    delay(200);

    if (Firebase.RTDB.setFloat(&fbdo, "/root/devices/casa_laura_adrian/status/temperature", temp)) {
      Serial.println("loop: Set temperature... ok\n\n");
      digitalWrite(LED_BUILTIN, LOW);
    } else {
      Serial.printf("loop: Set temperature... error %s\n\n", fbdo.errorReason().c_str());
      digitalWrite(LED_BUILTIN, HIGH);
    }
  }

  delay(periodMs);
}