#if defined(ESP32)
#include <WiFi.h>
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>

#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>
#define WIFI_SSID ""
#define WIFI_PASSWORD ""
#define API_KEY ""
#define DATABASE_URL ""
#define USER_EMAIL ""
#define USER_PASSWORD ""

const int BOILER_RELAY = D1;
const int WATERING_RELAY = D2;

//Define Firebase Data object
FirebaseData stream;
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;

int count = 0;

volatile bool dataChanged = false;
volatile bool wateringState = false;
volatile bool boilerState = false;
int periodMs = 60000;


void streamCallback(FirebaseStream data) {
  Serial.println("##########################streamCallback##########################\n");
  Serial.printf("sream path, %s\nevent path, %s\ndata type, %s\nevent type, %s\n\n",
                data.streamPath().c_str(),
                data.dataPath().c_str(),
                data.dataType().c_str(),
                data.eventType().c_str());
  printResult(data);  //see addons/RTDBHelper.h
  Serial.println();
  Serial.println("##################################################################\n");

  if (data.dataPath() == "/" && data.dataTypeEnum() == fb_esp_rtdb_data_type_json) {
    FirebaseJson &json = data.jsonObject();
    FirebaseJsonData wateringResult, boilerResult;
    json.get(wateringResult, "watering");
    json.get(boilerResult, "boiler");
    if (wateringResult.success && wateringResult.type == "boolean"
        && boilerResult.success && boilerResult.type == "boolean") {
      bool _watering = wateringResult.to<bool>();
      wateringState = wateringResult.to<bool>();
      boilerState = boilerResult.to<bool>();
      dataChanged = true;
    } else {
      Serial.println("streamCallback: error parsing json\n");
    }
  } else if (data.dataPath() == "/boiler" && data.dataTypeEnum() == fb_esp_rtdb_data_type_boolean) {
    boilerState = data.to<bool>();
    dataChanged = true;
  } else if (data.dataPath() == "/watering" && data.dataTypeEnum() == fb_esp_rtdb_data_type_boolean) {
    wateringState = data.to<bool>();
    dataChanged = true;
  } else {
    Serial.printf("streamCallback: unknown data type. dataPath: %s, dataTypeEnum: %d\n",
                  data.dataPath().c_str(), data.dataTypeEnum());
  }
}

void streamTimeoutCallback(bool timeout) {
  if (timeout)
    Serial.println("streamTimeoutCallback: stream timed out, resuming...\n");

  if (!stream.httpConnected())
    Serial.printf("streamTimeoutCallback: error code: %d, reason: %s\n\n",
                  stream.httpCode(), stream.errorReason().c_str());
}

void setup() {

  Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
  pinMode(BOILER_RELAY, OUTPUT);
  digitalWrite(BOILER_RELAY, HIGH);
  pinMode(WATERING_RELAY, OUTPUT);
  digitalWrite(WATERING_RELAY, HIGH);

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

  /* Assign the api key (required) */
  config.api_key = API_KEY;

  /* Assign the user sign in credentials */
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;

  /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback;  //see addons/TokenHelper.h

  //Or use legacy authenticate method
  //config.database_url = DATABASE_URL;
  //config.signer.tokens.legacy_token = "<database secret>";

  Firebase.begin(&config, &auth);

  Firebase.reconnectWiFi(true);

//Recommend for ESP8266 stream, adjust the buffer size to match your stream data size
#if defined(ESP8266)
  stream.setBSSLBufferSize(2048 /* Rx in bytes, 512 - 16384 */, 512 /* Tx in bytes, 512 - 16384 */);
#endif

  if (!Firebase.RTDB.beginStream(&stream, "/root/devices/casa_laura_adrian/status/outputs"))
    Serial.printf("setup: sream begin error, %s\n\n", stream.errorReason().c_str());

  Firebase.RTDB.setStreamCallback(&stream, streamCallback, streamTimeoutCallback);

  /** Timeout options, below is default config.

  //WiFi reconnect timeout (interval) in ms (10 sec - 5 min) when WiFi disconnected.
  config.timeout.wifiReconnect = 10 * 1000;

  //Socket begin connection timeout (ESP32) or data transfer timeout (ESP8266) in ms (1 sec - 1 min).
  config.timeout.socketConnection = 30 * 1000;

  //ESP32 SSL handshake in ms (1 sec - 2 min). This option doesn't allow in ESP8266 core library.
  config.timeout.sslHandshake = 2 * 60 * 1000;

  //Server response read timeout in ms (1 sec - 1 min).
  config.timeout.serverResponse = 10 * 1000;

  //RTDB Stream keep-alive timeout in ms (20 sec - 2 min) when no server's keep-alive event data received.
  config.timeout.rtdbKeepAlive = 45 * 1000;

  //RTDB Stream reconnect timeout (interval) in ms (1 sec - 1 min) when RTDB Stream closed and want to resume.
  config.timeout.rtdbStreamReconnect = 1 * 1000;

  //RTDB Stream error notification timeout (interval) in ms (3 sec - 30 sec). It determines how often the readStream
  //will return false (error) when it called repeatedly in loop.
  config.timeout.rtdbStreamError = 3 * 1000;

  */
}

void loop() {
  //Flash string (PROGMEM and FPSTR), Arduino String, C++ string, const char, char array, string literal are supported
  //in all Firebase and FirebaseJson functions, unless F() macro is not supported.

  if (Firebase.ready() && (millis() - sendDataPrevMillis > periodMs || sendDataPrevMillis == 0)) {
    digitalWrite(LED_BUILTIN, HIGH);
    delay(100);
    digitalWrite(LED_BUILTIN, LOW);
    delay(200);
    digitalWrite(LED_BUILTIN, HIGH);
    delay(100);
    digitalWrite(LED_BUILTIN, (wateringState || boilerState) ? LOW : HIGH);

    sendDataPrevMillis = millis();
    count++;
    FirebaseJson json;
    json.add("wateringState", wateringState);
    json.add("boilerState", boilerState);
    json.add("num", count);
    json.add("period", periodMs / 1000);
    json.add("heartbeat", Firebase.getCurrentTime());
    Serial.printf("loop: Set json... %s\n\n",
                  Firebase.RTDB.setJSON(&fbdo, "/root/devices/casa_laura_adrian/status/esp8266", &json)
                    ? "ok"
                    : fbdo.errorReason().c_str());
  }

  if (dataChanged) {
    dataChanged = false;
    Serial.println("loop: data changed\n");
    digitalWrite(LED_BUILTIN, (wateringState || boilerState) ? LOW : HIGH);
    digitalWrite(BOILER_RELAY, boilerState ? LOW : HIGH);
    digitalWrite(WATERING_RELAY, wateringState ? LOW : HIGH);
  }
}