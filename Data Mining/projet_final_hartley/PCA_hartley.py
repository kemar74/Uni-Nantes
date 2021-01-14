import matplotlib.patches as patches
import matplotlib.pyplot as plt
import numpy as np
import colorsys
from mpl_toolkits.mplot3d import Axes3D
from matplotlib.patches import FancyArrowPatch
from mpl_toolkits.mplot3d import proj3d

class Arrow3D(FancyArrowPatch):
    def __init__(self, xs, ys, zs, *args, **kwargs):
        FancyArrowPatch.__init__(self, (0,0), (0,0), *args, **kwargs)
        self._verts3d = xs, ys, zs

    def draw(self, renderer):
        xs3d, ys3d, zs3d = self._verts3d
        xs, ys, zs = proj3d.proj_transform(xs3d, ys3d, zs3d, renderer.M)
        self.set_positions((xs[0],ys[0]),(xs[1],ys[1]))
        FancyArrowPatch.draw(self, renderer)

# Based on fanalysis.pca :
def correlation_circle(pca, axis=[1, 2], groups_coef=None, originalData=None):
    fig = plt.figure()
    if len(axis) == 3:
        ax = fig.add_subplot(111, projection='3d')
    else:
        ax = fig.add_subplot(111, aspect="equal")

    series = []
    if pca.model_ == "mca":
        for axe in range(len(axis)):
            series.append(pca.col_coord_[:, axis[axe] - 1])
    else:
        for axe in range(len(axis)):
            series.append(pca.col_cor_[:, axis[axe] - 1])
    labels = pca.col_labels_

    for i in np.arange(0, series[0].shape[0]):
        # x = x_serie[i]
        # y = y_serie[i]
        label = labels[i]
        
        delta = 0.1 if series[1][i] >= 0 else -0.1
        if len(axis) == 2:
            ax.annotate("", xy=(series[0][i], series[1][i]), xytext=(0, 0),
                        arrowprops={"color": "red" if label in groups_coef or label == "country" else "black",
                                    "width": 1,
                                    "headwidth": 4,
                                    "alpha" : 0.3})
            ax.text(series[0][i], series[1][i] + delta, label,
                    horizontalalignment="center", verticalalignment="center",
                    color="red" if label in groups_coef or label == "country"  else "blue")
        else:
            a = Arrow3D([0, series[0][i]], [0, series[1][i]], 
                [0, series[2][i]], mutation_scale=20, 
                lw=3, arrowstyle="-|>", color="r")
            ax.add_artist(a)
            ax.text(series[0][i], series[1][i] + delta, series[2][i], label,
                    horizontalalignment="center", verticalalignment="center",
                    color="red" if label in groups_coef or label == "country"  else "blue")

    # plt.axvline(x=0, linestyle="--", linewidth=0.5, color="k")
    # plt.axhline(y=0, linestyle="--", linewidth=0.5, color="k")
    ax.add_artist(patches.Circle((0, 0), 1.0, color="black", fill=False))
    ax.set_xlim(-1.3, 1.3)
    ax.set_ylim(-1.3, 1.3)
    if len(axis) == 3:
        ax.set_zlim(-1.3, 1.3)
    plt.title("Correlation circle")
    plt.xlabel("Dim " + str(axis[0]) + " ("
                + str(np.around(pca.eig_[1, axis[0] - 1], 2)) + "%)")
    plt.ylabel("Dim " + str(axis[1]) + " ("
                + str(np.around(pca.eig_[1, axis[1] - 1], 2)) + "%)")
    if len(axis) == 3:
        ax.set_zlabel("Dim " + str(axis[2]) + " ("
                    + str(np.around(pca.eig_[1, axis[2] - 1], 2)) + "%)")
    plt.show()
    
def mapping_row(pca, axis = [1, 2], colorOnCountryGroup= False, displayLabel = True, groups_coef=None, originalData=None, colors=[]):
    fig = plt.figure()
    if len(axis) == 3:
        ax = fig.add_subplot(111, projection='3d')
    else:
        ax = fig.add_subplot(111, aspect="equal")

    series = []
    for axe in range(len(axis)):
        series.append(pca.row_coord_[:, axis[axe] - 1])
    labels = pca.col_labels_
    if colors == [] :
        colors = ['blue'] * pca.row_coord_.shape[0]
    if colorOnCountryGroup:
        minColor = min(originalData['country_group_id'])
        maxColor = max(originalData['country_group_id'])
        for i in range(originalData.shape[0]):
            colors[i] = colorsys.hsv_to_rgb((originalData['country_group_id'][i] - minColor)/(1 + maxColor-minColor), 1, 1)

    x = series[0]
    y = series[1]
    if len(axis) == 3:
        z = series[2]
        ax.scatter(x, y, z, marker='.', color=colors, alpha=0.5)
    else:
        ax.scatter(x, y, marker=".", color=colors, alpha = 0.5)
    for i in np.arange(0, pca.row_coord_.shape[0]):
        if displayLabel :
            if len(axis) == 3:
                ax.text(pca.row_coord_[i, axis[0] - 1],
                         pca.row_coord_[i, axis[1] - 1],
                         pca.row_coord_[i, axis[2] - 1],
                         pca.row_labels_[i],
                         horizontalalignment="center", verticalalignment="center",
                         color="red" if colorOn and originalData['country'][i] == colorOn else "blue")
            else:
                ax.text(pca.row_coord_[i, axis[0] - 1],
                         pca.row_coord_[i, axis[1] - 1],
                         pca.row_labels_[i],
                         horizontalalignment="center", verticalalignment="center",
                         color="red" if colorOn and originalData['country'][i] == colorOn else "blue")
    plt.title("Factor map for rows")
    plt.xlabel("Dim " + str(axis[0]) + " ("
                + str(np.around(pca.eig_[1, axis[0] - 1], 2)) + "%)")
    plt.ylabel("Dim " + str(axis[1]) + " ("
                + str(np.around(pca.eig_[1, axis[1] - 1], 2)) + "%)")
    if len(axis) == 3:
        ax. set_zlabel("Dim " + str(axis[2]) + " ("
                + str(np.around(pca.eig_[1, axis[2] - 1], 2)) + "%)")
    # plt.axvline(x=0, linestyle="--", linewidth=0.5, color="k")
    # plt.axhline(y=0, linestyle="--", linewidth=0.5, color="k")
    plt.show()

def mapping_col(pca, num_x_axis, num_y_axis, short_labels=True, groups_coef=None, originalData=None):
    
    if pca.model_ == "mca" and short_labels:
        col_labels = pca.col_labels_short_
    else:
        col_labels = pca.col_labels_
    plt.scatter(pca.col_coord_[:, num_x_axis - 1],
                pca.col_coord_[:, num_y_axis - 1],
                marker=".", color="white")
    for i in np.arange(0, pca.col_coord_.shape[0]):
        plt.text(pca.col_coord_[i, num_x_axis - 1],
                 pca.col_coord_[i, num_y_axis - 1],
                 col_labels[i],
                 horizontalalignment="center", verticalalignment="center",
                 color="blue")
    plt.title("Factor map for columns")
    plt.xlabel("Dim " + str(num_x_axis) + " ("
                + str(np.around(pca.eig_[1, num_x_axis - 1], 2)) + "%)")
    plt.ylabel("Dim " + str(num_y_axis) + " ("
                + str(np.around(pca.eig_[1, num_y_axis - 1], 2)) + "%)")
    plt.axvline(x=0, linestyle="--", linewidth=0.5, color="k")
    plt.axhline(y=0, linestyle="--", linewidth=0.5, color="k")
    plt.show()

def mapping(pca, num_x_axis, num_y_axis, short_labels=True, groups_coef=None, originalData=None):
    plt.figure()
    if pca.model_ == "mca" and short_labels:
        col_labels = pca.col_labels_short_
    else:
        col_labels = pca.col_labels_
    plt.scatter(pca.row_coord_[:, num_x_axis - 1],
                pca.row_coord_[:, num_y_axis - 1],
                marker=".", color="white")
    plt.scatter(pca.col_coord_[:, num_x_axis - 1],
                pca.col_coord_[:, num_y_axis - 1],
                marker=".", color="white")
    for i in np.arange(0, pca.row_coord_.shape[0]):
        plt.text(pca.row_coord_[i, num_x_axis - 1],
                 pca.row_coord_[i, num_y_axis - 1],
                 pca.row_labels_[i],
                 horizontalalignment="center", verticalalignment="center",
                 color="red")
    for i in np.arange(0, pca.col_coord_.shape[0]):
        plt.text(pca.col_coord_[i, num_x_axis - 1],
                 pca.col_coord_[i, num_y_axis - 1],
                 col_labels[i],
                 horizontalalignment="center", verticalalignment="center",
                 color="blue")
    plt.title("Factor map")
    plt.xlabel("Dim " + str(num_x_axis) + " ("
                + str(np.around(pca.eig_[1, num_x_axis - 1], 2)) + "%)")
    plt.ylabel("Dim " + str(num_y_axis) + " ("
                + str(np.around(pca.eig_[1, num_y_axis - 1], 2)) + "%)")
    plt.axvline(x=0, linestyle="--", linewidth=0.5, color="k")
    plt.axhline(y=0, linestyle="--", linewidth=0.5, color="k")
    plt.show()
