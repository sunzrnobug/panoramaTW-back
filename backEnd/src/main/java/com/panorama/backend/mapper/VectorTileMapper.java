package com.panorama.backend.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * @author: DMK
 * @description:
 * @date: 2024-09-02 14:30:43
 * @version: 1.0
 */
@Mapper
public interface VectorTileMapper {

    Object getVectorTile(String tableName, int z, int x, int y, String[] visualizationFieldsList);

    Map<String, Object> getDetailInfo(String tableName, int ogc_fid, String[] detailFieldsList);

    void insertGeoJsonFeature(String tableName, String geometry, Map<String, Object> properties);

    void createTable(String tableName, Map<String, Object> propertyType, String type, int srid);

    void deleteTable(String tableName);

    int findSRIDByWKT(String wkt);

    int getSameCount(String tableName);

}