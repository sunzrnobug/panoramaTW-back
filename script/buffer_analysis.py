import geopandas as gpd
import json
import argparse
import sys
from shapely.geometry import shape

def buffer_geojson(geojson_str, buffer_distance):
    """
    计算 GeoJSON 字符串的缓冲区，并返回新的 GeoJSON 字符串
    :param geojson_str: 输入的 GeoJSON 字符串
    :param buffer_distance: 缓冲区距离（单位：米）
    :return: 计算后的 GeoJSON 字符串
    """
    try:
        # 解析 GeoJSON 字符串
        geojson_dict = json.loads(geojson_str)
        
        # 检查是否为单个几何体
        if "type" in geojson_dict and "features" not in geojson_dict:
            # 将单个几何体转换为 FeatureCollection
            geojson_dict = {
                "type": "FeatureCollection",
                "features": [{
                    "type": "Feature",
                    "geometry": geojson_dict,
                    "properties": {}
                }]
            }
            
        # 提取 features 并转换为 GeoDataFrame，设置为 WGS84 坐标系
        gdf = gpd.GeoDataFrame.from_features(geojson_dict["features"], crs="EPSG:4326")
        
        # 转换到投影坐标系（Web墨卡托）进行缓冲区计算
        gdf_projected = gdf.to_crs("EPSG:3857")
        
        # 在投影坐标系中计算缓冲区（单位：米）
        gdf_projected["geometry"] = gdf_projected["geometry"].buffer(buffer_distance)
        
        # 转回 WGS84 坐标系
        gdf_buffered = gdf_projected.to_crs("EPSG:4326")

        # 转换回 GeoJSON 格式，确保使用 ASCII 编码
        return json.dumps(json.loads(gdf_buffered.to_json()), ensure_ascii=True)
    except Exception as e:
        return json.dumps({"error": str(e)}, ensure_ascii=True)


def main():
    parser = argparse.ArgumentParser(description="计算 GeoJSON 数据的缓冲区")
    parser.add_argument("--input_geojson", type=str, help="输入的 GeoJSON 字符串或文件路径", required=True)
    parser.add_argument("--buffer_distance", type=float, help="缓冲区距离", required=True)

    args = parser.parse_args()

    # 读取 GeoJSON 数据
    try:
        # 如果是文件路径，则读取文件内容
        if args.input_geojson.endswith(".geojson") or args.input_geojson.endswith(".json"):
            with open(args.input_geojson, "r", encoding="utf-8") as f:
                geojson_str = f.read()
        else:
            geojson_str = args.input_geojson

        # 计算缓冲区
        result = buffer_geojson(geojson_str, args.buffer_distance)

        # 直接输出结果，不做额外的编码处理
        print(result, end='', flush=True)

    except Exception as e:
        print(json.dumps({"error": str(e)}, ensure_ascii=True), file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
