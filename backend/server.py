from flask import Flask
from flask import Response
from flask import request
import pandas as pd
import xgboost as xgb
import numpy as np
from pandas import DataFrame as df
import math
from sklearn.externals import joblib


app = Flask(__name__)
import os.path

shots = {'1':'Serve', '2':'Smash', '3':'Right Drop', '4':'Left Drop', '5':'Clear'}


@app.route('/saveTrainingData', methods=['POST'])
def save_training_data():
        request_str = request.data.decode("utf-8")
        shot_type = request_str.split('\n', 1)[0]
        str_to_write = request_str[len(shot_type):]
        print(str_to_write)
        file_num = 0
        while (os.path.isfile(shot_type + str(file_num) + ".csv")):
                file_num += 1
        f = open(shot_type + str(file_num) + ".csv", "w")
        f.write(str_to_write)
        f.close()
        return "good!"

@app.route('/evaluateData', methods=['POST'])
def evaluate_data():
        request_str = request.data.decode("utf-8")
        print(request_str)

        f = open("evaluate" + ".csv", "w")
        f.write(request_str)
        f.close()
        processed_data = format_row(pd.read_csv("evaluate.csv"))
        model = joblib.load('../model.pkl')
        results = model.predict(processed_data)
        print(shots[str(int(results[0]))])
        print(results[0])
        os.remove("evaluate.csv")
        return shots[str(int(results[0]))]
    
def format_row(temp_df):
    ultimate_df = np.zeros(120)
    leng = math.floor(temp_df.shape[0]/10)
    # go through all ten deciles
    for i in range(10):
        # first 9 are all same size
        if (i < 9):
            # for each variable (acc_[xyz], gyro_[xyz]), find mean and std
            for j in range(6):
                ultimate_df[i*12+j] = temp_df.iloc[leng*i:leng*(i+1), :].mean()[j]
                ultimate_df[i*12+j+6] = temp_df.iloc[leng*i:leng*(i+1), :].std()[j]  

        # for working with the last (irregularily sized) decile
        else:
            # for each variable (acc_[xyz], gyro_[xyz]), find mean and std
            for j in range(6):
                ultimate_df[i*12+j] = temp_df.iloc[leng*i: ,: ].mean()[j]
                ultimate_df[i*12+j+6] = temp_df.iloc[leng*i: ,: ].std()[j]
                
    return_this = pd.DataFrame(ultimate_df).T
    print(return_this.shape)
    print(return_this)
    return return_this
