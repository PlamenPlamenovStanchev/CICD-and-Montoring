from flask import Flask
import redis

app = Flask(__name__)

cache = redis.Redis(host='localhost', port=6379)

@app.route('/')
def hello():
    count = cache.incr('hits')
    return f'Hello! This Python app has been viewed {count} times.\n'

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
