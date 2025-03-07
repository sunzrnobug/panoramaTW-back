package com.panorama.backend.util;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: : Steven Da
 * @date:  2024/10/04/18:12
 * @description:
 */
public class ShapeFileUtil {
    public static Map<String, String> parseShapefile(File shapefile) throws IOException {
        // 存储字段信息
        Map<String, String> fieldInfo = new HashMap<>();

        FileDataStore store = FileDataStoreFinder.getDataStore(shapefile);

        // 2. 获取Shapefile的FeatureSource
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // 3. 获取Schema（Feature Type）
        SimpleFeatureType schema = featureSource.getSchema();

        // 4. 获取字段（属性）的名称列表
        List<AttributeDescriptor> descriptors = schema.getAttributeDescriptors();

        for (AttributeDescriptor descriptor : descriptors) {
            String fieldName = descriptor.getLocalName();
            String fieldType = descriptor.getType().getBinding().getSimpleName();
            fieldInfo.put(fieldName, fieldType);
        }

        store.dispose(); // 关闭资源
        return fieldInfo;
    }
}
