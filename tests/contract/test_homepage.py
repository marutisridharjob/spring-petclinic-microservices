import requests

def test_home_page():
    url = "http://localhost:8080/owners"
    r = requests.get(url)
    print("Status code:", r.status_code)
    assert r.status_code == 200
