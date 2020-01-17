import pandas as pd
import matplotlib.pyplot as plt
import sklearn.feature_extraction as fe

limit = -1

data = pd.read_csv('reviews_by_course.csv')
if limit > 0:
	data = data.head(limit)

# print(data.groupby("Label").size())
# data.hist(column = "Label")
# plt.show()

# txt = ["Coucou ca va ou bien",
	# "Salut c'est cool",
	# "Non ca va pas aller"]

# array = [[1 if line == course else 0 for course in list(dict.fromkeys(data["CourseId"].values)) ] for line in data["CourseId"].values]
# print(array)

balanceddata = dict()
for i in range(1, 6):
	balanceddata[i] = data[data.Label == i].sample(200)
balanced_subset = pd.concat(balanceddata.values())
print(balanced_subset.head(50))
# vectorizer = fe.text.CountVectorizer()
# courses = vectorizer.fit_transform(data.head(10)["CourseId"].values).toarray()
# print("Voc : ", vectorizer.get_feature_names())
# for i in range(len(courses)) :
# 	print(courses[i])

# txt = data.head(10)["Review"].values
# Xr = vectorizer.fit_transform(txt).toarray()

# print("Voc : ", vectorizer.get_feature_names())
# for i in range(len(txt)) :
# 	print(txt[i], Xr[i])