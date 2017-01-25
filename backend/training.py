
# coding: utf-8

# In[24]:

import csv
import glob
import pandas as pd
import xgboost as xgb
import numpy as np
from pandas import DataFrame as df
import math
import sklearn as skl
from sklearn import model_selection
from sklearn.externals import joblib


# In[25]:

# format row takes in a dataframe and a label (0:serve, 1:smash, 2:right_drop, 3:left_drop, 4:clear)
def format_row(temp_df, label):
    ultimate_df = np.zeros(121)
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
                
    ultimate_df[120] = label+1
    return_this = pd.DataFrame(ultimate_df).T
    #print(return_this.shape)
    return return_this


# In[26]:

path = 'battingCompanion/' # use your path
hit_types = ['serve', 'smash', 'right_drop', 'left_drop', 'clear']

#get file names to read in later

file_list = [0]*5
file_list[0] = glob.glob(path + 'serve' + "*.csv")
file_list[1] = glob.glob(path + 'smash' + "*.csv")
file_list[2] = glob.glob(path + 'right_drop' + "*.csv")
file_list[3] = glob.glob(path + 'left_drop' + "*.csv")
file_list[4] = glob.glob(path + 'clear' + "*.csv")

final_df = pd.DataFrame()

for i in range(5):
    for entry in file_list[i]:
        final_df = final_df.append(format_row(pd.read_csv(entry), i))


# In[27]:

final_df


# In[28]:

#serve_label = final_df.pop(120)
#smash_label = final_df.pop(121)
#right_drop_label = final_df.pop(122)
#left_drop_label = final_df.pop(123)
#clear_label = final_df.pop(124)
labels = final_df.pop(120)


# In[29]:

# xgboost with skl gridsearch
cv_params = {'max_depth': [3,5,7], 'min_child_weight': [1,3,5]}
ind_params = {'learning_rate': 0.3, 'n_estimators': 1000, 'seed':0, 'subsample': 0.8, 'colsample_bytree': 0.8, 
             'objective': 'binary:logistic'}
optimized_GBM = model_selection.GridSearchCV(xgb.XGBClassifier(**ind_params), 
                            cv_params, 
                             scoring = 'accuracy', verbose=True, cv = 5, n_jobs = 1)


# In[30]:

optimized_GBM.fit(final_df, labels)
optimized_GBM.cv_results_


# In[31]:

joblib.dump(optimized_GBM.best_estimator_, 'model.pkl')

test22 = optimized_GBM.predict(final_df)


# In[29]:

for i in range(len(test22)):
    print("predict:" + str(test22[i]) + ", actual:" + str(serve_label[i]))


# In[34]:

print(serve_label[0])
print(test22)


# In[ ]:



