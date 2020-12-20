from flask import Flask, render_template, Response, jsonify

import redis

app = Flask(__name__)
r = redis.StrictRedis(host='127.0.0.1', port=6379, db=0)


@app.route('/')
def line():
    return render_template('index.html', title='Real-Time Topic Detection Twitter di Indonesia')

# @app.route('/')
# def show_homepage():
#   #Word Cloud = cloud.html and app-cloud.js
#     return render_template("cloud.html")

def event_stream():
    pubsub = r.pubsub()
    pubsub.subscribe('TwitterLDAStream')
    for message in pubsub.listen():
        print(message)
        yield 'data: %s\n\n' % message['data']


@app.route('/basic')
def show_basic():
    return render_template("basic.html")

@app.route('/stream')
def stream():
    return Response(event_stream(), mimetype="text/event-stream")
    
if __name__ == '__main__':
    app.run(threaded=True,
    host='0.0.0.0'
)
