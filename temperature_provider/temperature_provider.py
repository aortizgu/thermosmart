import glob
import time
import requests

base_dir = '/sys/bus/w1/devices/'
device_folder = glob.glob(base_dir + '28*')[0]
device_file = device_folder + '/w1_slave'

def read_temp_raw():
    f = open(device_file, 'r')
    lines = f.readlines()
    f.close()
    return lines

def read_temp():
    lines = read_temp_raw()
    while lines[0].strip()[-3:] != 'YES':
        time.sleep(0.2)
        lines = read_temp_raw()
    equals_pos = lines[1].find('t=')
    if equals_pos != -1:
        temp_string = lines[1][equals_pos+2:]
        temp_c = float(temp_string) / 1000.0
        return temp_c

# login and fetch token
user = ""
password = ""
login_payload = {
        "email": user,
        "password": password,
        "returnSecureToken": True
}
login_params = {
    "key": "AIzaSyCCNhYVqbU_9YAZpe5y2OKzXWdkPIHBorM"
}
login_reply = requests.post("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword", params=login_params, data=login_payload)
login_reply_json = login_reply.json()
token = login_reply_json["idToken"]

# update temp
temp = read_temp()
update_temp_payload = {
  "temperature": temp
}
update_temp_params = {
    "auth": token
}
requests.patch('https://thermosmart-b5382-default-rtdb.europe-west1.firebasedatabase.app/root/devices/casa_laura_adrian/status.json', params=update_temp_params, json=update_temp_payload)