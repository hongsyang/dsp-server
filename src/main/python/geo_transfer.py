#!/usr/bin/python
# coding=utf-8
# Created by wanght on 2017/09/21

from pyproj import Geod

import sys

"""
az az2 角度参数使用方法：
坐标转换算法，从 Y 轴 作为 0 度，顺时针依次是 45 90 度，往下就是 135 180 度
以 Y 轴作为0 度， 逆时针依次是 -45  ,-90  -135 -180 度
所以角度参数 范围是  0 ~ 180  -180 ~ 0 度
"""

def convert(lng , lat ,distance ,az ):
    des = [0.0,0.0]
    g = Geod(ellps="WGS84")
    des[0], des[1], backaz = g.fwd(lng, lat, az, distance)
    return des


# 参考类库： http://jswhit.github.io/pyproj/pyproj.Geod-class.html
def convert_leftdown_rightup(lng , lat ,distance ):
    az = 45.0
    leftdown = [0.0,0.0]
    rightup = [0.0,0.0]
    g = Geod(ellps="WGS84")
    leftdown[0], leftdown[1], backaz = g.fwd(lng, lat, az, distance)

    az2 = -135.0
    rightup[0] , rightup[1] , backaz = g.fwd(lng,lat,az2,distance)

    return leftdown,rightup


def convert_rightup(lng , lat ,distance ):

    leftdown = [0.0,0.0]
    rightup = [0.0,0.0]
    g = Geod(ellps="WGS84")
    az2 = -135.0
    rightup[0] , rightup[1] , backaz = g.fwd(lng,lat,az2,distance)

    return rightup


if __name__ == '__main__':
     lng = sys.argv[1]
     lat = sys.argv[2]
     distance = sys.argv[3]

     leftdown,rightup = convert_leftdown_rightup(lng,lat,distance)
     print str(leftdown[0]) + ',' + str(leftdown[1]) + '\t' + str(rightup[0]) + ',' + str(rightup[1])

if __name__ == '__main__2':
    coords = [

'112.9938068	28.23323918   ',
    ]
    for coord in coords:
        lnglat = coord.split('\t')
        lng = lnglat[0]
        lat = lnglat[1]
        des_coord = convert(lng,lat,1000,0)
        print('new BMap.Point(' + str(lnglat[0]) + ',' + str(lnglat[1]) + '),')
        print('new BMap.Point(' + str(des_coord[0]) + ',' + str(des_coord[1]) + '),')
        des_coord = convert(lng,lat,1000,180)
        print('new BMap.Point(' + str(des_coord[0]) + ',' + str(des_coord[1]) + '),')
        des_coord = convert(lng,lat,1000,-180)
        print('new BMap.Point(' + str(des_coord[0]) + ',' + str(des_coord[1]) + '),')

    # 这是一个坐标类库，支持 坐标 A 不同角度上的，距离为 400 米的坐标 B 的计算。
    # lng =126.52981820674619
    # lat = 45.79825360892112
    # distance = 1070
    # geo = convert(lng,lat,distance,0)
    # print geo

    #
    # geo_list = [
    #     '',
    #     '',
    # ]
    # for
    # distance = 400
    # leftdown , rightup = convert_leftdown_rightup(lng,lat,400)
    # print leftdown , rightup

    #
    # # g = Geod(ellps='clrk66') # Use Clarke 1966 ellipsoid.
    # g = Geod(ellps="WGS84") # Use Clarke 1966 ellipsoid.
    # # specify the lat/lons of some cities.,
    # lng1 = 116.360315
    # lat1 = 40.028804
    # lng2 = 116.365018
    # lat2 = 40.028825
    #
    # # compute forward and back azimuths, plus distance
    # # between Boston and Portland.
    # az12,az21,dist = g.inv(lng1,lat1,lng2,lat2)
    # print str(az12) + ' ' +  str(az21) + ' ' + str(dist)
    #
    # lng2, lat2, backaz = g.fwd(lng1, lat1, az12, dist)
    #
    # print  lng2 , lat2,backaz
    # # # 读取经纬度
    # # lon = 116.366
    # # lat = 39.8673
    # # x , y = convert(lon,lat,1000)
    # # print x, y
    # # lng2 ,lat2 = reverse(x,y)
    # # print lng2, lat2