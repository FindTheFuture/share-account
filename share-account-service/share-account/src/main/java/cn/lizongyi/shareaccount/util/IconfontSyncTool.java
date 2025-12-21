package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.dao.ClassEntityMapper;
import cn.lizongyi.shareaccount.entity.ClassEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 同步 iconfont.json 至数据库 `class` 表的工具类。
 * 功能：
 * 1) 读取指定路径的 iconfont.json（可自定义路径），解析其中的 name 与 font_class；
 * 2) 从数据库读取 `class` 表中所有有效记录的 name；
 * 3) name 匹配则将对应的 font_class 写入 `class.icon` 字段。
 */
@Slf4j
@Component
public class IconfontSyncTool {

    @Resource
    private ClassEntityMapper classEntityMapper;

    /**
     * 从指定的 iconfont.json 路径读取并同步到数据库。
     * @param jsonFilePath iconfont.json 文件的绝对路径
     * @return 成功更新的记录数
     */
    public int syncIconToClassTable(String jsonFilePath) {
        if (jsonFilePath == null || jsonFilePath.trim().isEmpty()) {
            log.warn("同步失败：jsonFilePath 为空");
            return 0;
        }

        try {
            // 1) 读取文件内容
            String jsonContent = Files.readString(Path.of(jsonFilePath));
            if (jsonContent == null || jsonContent.isEmpty()) {
                log.warn("同步失败：文件无内容 - {}", jsonFilePath);
                return 0;
            }

            // 2) 解析 iconfont.json -> name -> font_class 映射
            Map<String, String> nameToFontClass = parseIconfontJson(jsonContent);
            if (nameToFontClass.isEmpty()) {
                log.warn("同步失败：未解析到任何图标信息");
                return 0;
            }

            // 3) 查询数据库中的分类（仅 status=0）
            List<ClassEntity> classList = classEntityMapper.selectAll();
            if (classList == null || classList.isEmpty()) {
                log.info("数据库中无可更新分类记录");
                return 0;
            }

            // 4) 匹配并更新 icon 字段
            int updated = 0;
            for (ClassEntity entity : classList) {
                String name = entity.getName();
                if (name == null || name.isEmpty()) {
                    continue;
                }
                String fontClass = nameToFontClass.get(name);
                if (fontClass == null || fontClass.isEmpty()) {
                    continue;
                }
                // 仅在变化时更新，避免不必要写入
                if (!fontClass.equals(entity.getIcon())) {
                    entity.setIcon(fontClass);
                    try {
                        int rows = classEntityMapper.updateById(entity);
                        if (rows > 0) {
                            updated++;
                        } else {
                            log.warn("更新失败：id={} name={} icon={}", entity.getId(), name, fontClass);
                        }
                    } catch (Exception e) {
                        log.error("更新异常：id={} name={} icon={} err={}", entity.getId(), name, fontClass, e.getMessage());
                    }
                }
            }

            log.info("同步完成：总记录={}，匹配更新={}", classList.size(), updated);
            return updated;
        } catch (Exception e) {
            log.error("同步过程异常：{}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 解析 iconfont.json 内容，抽取 name -> font_class 映射。
     * 兼容常见结构：顶层包含 `glyphs` 数组；或其它数组中包含 `name`、`font_class` 字段。
     */
    private Map<String, String> parseIconfontJson(String jsonContent) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode root = JacksonUtils.readTree(jsonContent);
            if (root == null || root.isMissingNode()) {
                return result;
            }

            // 优先处理常见的 glyphs 数组结构
            JsonNode glyphs = root.get("glyphs");
            if (glyphs != null && glyphs.isArray()) {
                for (JsonNode node : glyphs) {
                    String name = getText(node, "name");
                    String fontClass = getText(node, "font_class");
                    if (hasText(name) && hasText(fontClass)) {
                        result.put(name, fontClass);
                    }
                }
            }

            // 若未解析到，尝试遍历所有数组字段，寻找包含 name 与 font_class 的对象
            if (result.isEmpty()) {
                Iterator<String> fieldNames = root.fieldNames();
                while (fieldNames.hasNext()) {
                    String field = fieldNames.next();
                    JsonNode value = root.get(field);
                    if (value != null && value.isArray()) {
                        for (JsonNode node : value) {
                            String name = getText(node, "name");
                            String fontClass = getText(node, "font_class");
                            if (hasText(name) && hasText(fontClass)) {
                                result.put(name, fontClass);
                            }
                        }
                    }
                }
            }

            // 再次兜底：若 JSON 直接是数组
            if (result.isEmpty() && root.isArray()) {
                for (JsonNode node : root) {
                    String name = getText(node, "name");
                    String fontClass = getText(node, "font_class");
                    if (hasText(name) && hasText(fontClass)) {
                        result.put(name, fontClass);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析 iconfont.json 异常：{}", e.getMessage(), e);
        }
        return result;
    }

    private static String getText(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        return v == null ? null : v.asText();
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}