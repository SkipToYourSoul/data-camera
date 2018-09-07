package com.stemcloud.liye.dc.simulator;

import com.stemcloud.liye.dc.dao.MysqlRepository;
import com.stemcloud.liye.dc.domain.SensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Belongs to data-camera-server
 * Description:
 *  data generator
 * @author liye on 2018/8/19
 */
public class DataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGenerator.class);

    private static int flag = 0;
    private static List<String> lines = new ArrayList<>();

    public static void init() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./random.dat")));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("init data generator, data size = {}, flag = {}", lines.size(), flag);
    }

    public static Map<String, Double> getRocket(List<String> dim) {
        Map<String, Double> result = new HashMap<>();
        String recorder = lines.get(flag);

        String[] values = recorder.split("\t");
        if (values.length == 15) {
            for (int i = 0; i < dim.size(); i++) {
                result.put(dim.get(i), Double.valueOf(values[i]));
            }
        }
        // LOGGER.info("Get {} recorders, size = {}, dim size = {}", flag, values.length, dim.size());

        if (flag == lines.size() - 1) {
            flag = 0;
        } else {
            flag++;
        }

        return result;
    }

    public static Map<String, Double> generateRandom(SensorConfig config) {
        Map<String, Double> values = new HashMap<>();
        if (config != null) {
            // 获取数据维度
            List<String> dimension = config.getDimension();
            dimension.forEach(dim -> {
                if ("视频".equals(dim)) {
                    return;
                }
                double val;
                if("温度".equals(dim)){
                    val = rand(10, 25);
                }else if ("湿度".equals(dim)){
                    val = rand(30, 60);
                }else if("光照".equals(dim)){
                    val = rand(30, 500);
                }else if ("脑电波".equals(dim)){
                    val = rand(100, 1600);
                }else if ("压力".equals(dim)){
                    val = rand(1, 30);
                }else {
                    val = rand(1, 100);
                }
                values.put(dim, val);
            });
        }

        return values;
    }

    private static double rand(int from, int to){
        if (to <= from){
            return 0.0;
        }
        Random random = new Random();
        return random.nextDouble() * from + (to - from);
    }

    public static void main(String[] args) {
        // 模拟完整数据生成
        String dim = "相对高度;X轴加速度;Y轴加速度;Z轴加速度;气压;X轴角速度;Y轴角速度;Z轴角速度;X轴磁力;Y轴磁力;Z轴磁力;航向;横滚角;俯仰角;温度";
        List<String> dims = Arrays.asList(dim.split(";"));
        long beginTime = System.currentTimeMillis();

        DataGenerator.init();
        for (int i = 0; i < 90; i++) {
            Map<String, Double> values = DataGenerator.getRocket(dims);
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                MysqlRepository.saveValueData(8, 44, entry.getKey(), entry.getValue(), beginTime);
            }
            beginTime += 100;
        }
    }
}
