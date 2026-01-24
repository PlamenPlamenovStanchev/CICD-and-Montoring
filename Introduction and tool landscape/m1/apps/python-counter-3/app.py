from flask import Flask
import mysql.connector

app = Flask(__name__)

cnx = mysql.connector.connect(user='app_user',
                              password='Parolka-12345',
                              host='localhost', 
                              database='counters')

@app.route('/')
def hello():
    cursor = cnx.cursor()
    cursor.execute("INSERT INTO hits () VALUES ();")
    cnx.commit()
    cursor.execute("SELECT COUNT(*) FROM hits;")
    (count,) = cursor.fetchone()
    cursor.close()

    return f'Hello! This Python app has been viewed {count} times.\n'


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
    cnx.close()