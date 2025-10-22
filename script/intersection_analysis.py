import geopandas as gpd
import json
import argparse
import sys
from shapely.geometry import shape

def intersection_geojson(geojson_str1, geojson_str2):
    """
    计算两个 GeoJSON 字符串的相交部分，并返回新的 GeoJSON 字符串
    :param geojson_str1: 第一个 GeoJSON 字符串
    :param geojson_str2: 第二个 GeoJSON 字符串
    :return: 相交部分的 GeoJSON 字符串
    """
    try:
        # 解析 GeoJSON 字符串为 Python 字典
        geojson_dict1 = json.loads(geojson_str1)
        geojson_dict2 = json.loads(geojson_str2)
        
        # 检查并转换单个几何体为 FeatureCollection
        for geojson_dict in [geojson_dict1, geojson_dict2]:
            if "type" in geojson_dict and "features" not in geojson_dict:
                geojson_dict.update({
                    "type": "FeatureCollection",
                    "features": [{
                        "type": "Feature",
                        "geometry": geojson_dict.copy(),
                        "properties": {}
                    }]
                })
        
        # 提取 features 并转换为 GeoDataFrame
        gdf1 = gpd.GeoDataFrame.from_features(geojson_dict1["features"])
        gdf2 = gpd.GeoDataFrame.from_features(geojson_dict2["features"])
        
        # 检查是否为点和多边形的情况
        if 'Point' in gdf1.geometry.type.values and 'Polygon' in gdf2.geometry.type.values:
            # 使用 sjoin 来找出在多边形内的点
            result = gpd.sjoin(gdf1, gdf2, how="inner", predicate="within")
            return result.to_json()
        elif 'Point' in gdf2.geometry.type.values and 'Polygon' in gdf1.geometry.type.values:
            # 使用 sjoin 来找出在多边形内的点
            result = gpd.sjoin(gdf2, gdf1, how="inner", predicate="within")
            return result.to_json()
        else:
            # 对于其他情况使用原来的 overlay
            intersection_gdf = gpd.overlay(gdf1, gdf2, how="intersection")
            return intersection_gdf.to_json()
            
    except Exception as e:
        return json.dumps({"error": str(e)})

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    parser = argparse.ArgumentParser(description="计算两个 GeoJSON 数据的相交部分")
    parser.add_argument("--input_geojson_1", type=str, help="第一个输入的 GeoJSON 字符串或文件路径", required=True)
    parser.add_argument("--input_geojson_2", type=str, help="第二个输入的 GeoJSON 字符串或文件路径", required=True)

    args = parser.parse_args()

    try:
        # 读取第一个 GeoJSON 数据
        if args.input_geojson_1.endswith((".geojson", ".json")):
            with open(args.input_geojson_1, "r", encoding="utf-8") as f:
                geojson_str1 = f.read()
        else:
            geojson_str1 = args.input_geojson_1.strip("\"")

        # 读取第二个 GeoJSON 数据
        if args.input_geojson_2.endswith((".geojson", ".json")):
            with open(args.input_geojson_2, "r", encoding="utf-8") as f:
                geojson_str2 = f.read()
        else:
            geojson_str2 = args.input_geojson_2.strip("\"")

        # 计算相交部分
        result = intersection_geojson(geojson_str1, geojson_str2)

        # 打印结果到标准输出
        print(result)
        sys.stdout.flush()

    except Exception as e:
        print(json.dumps({"error": str(e)}), file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
