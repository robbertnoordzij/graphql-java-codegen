package com.kobylynskyi.graphql.codegen;

import com.kobylynskyi.graphql.codegen.model.DataModelFields;
import com.kobylynskyi.graphql.codegen.model.GeneratedLanguage;
import com.kobylynskyi.graphql.codegen.model.MappingContext;
import com.kobylynskyi.graphql.codegen.model.exception.UnableToCreateFileException;
import com.kobylynskyi.graphql.codegen.utils.Utils;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;

/**
 * Utility class for generating files
 *
 * @author kobylynskyi
 */
class GraphQLCodegenFileCreator {

    private static final String EXTENSION = ".java";
    private static final String EXTENSION_SCALA = ".scala";

    private GraphQLCodegenFileCreator() {
    }

    static File generateFile(MappingContext mappingContext, String templateName, Map<String, Object> dataModel, File outputDir) {
        GeneratedLanguage language = mappingContext.getGeneratedLanguage();
        String fileName = getGeneratedFileName(dataModel, language);
        File fileOutputDir = getFileTargetDirectory(dataModel, outputDir);
        File javaSourceFile = new File(fileOutputDir, fileName);
        try {
            if (!javaSourceFile.createNewFile()) {
                throw new FileAlreadyExistsException("File already exists: " + javaSourceFile.getPath());
            }
            Template template = FreeMarkerTemplatesRegistry.getTemplateWithLang(language, templateName);
            template.process(dataModel, new FileWriter(javaSourceFile));
        } catch (Exception e) {
            throw new UnableToCreateFileException(e);
        }
        return javaSourceFile;
    }

    private static String getGeneratedFileName(Map<String, Object> dataModel, GeneratedLanguage language) {
        String fileName;
        if (language.equals(GeneratedLanguage.SCALA)) {
            fileName = dataModel.get(DataModelFields.CLASS_NAME) + EXTENSION_SCALA;
        } else {
            fileName = dataModel.get(DataModelFields.CLASS_NAME) + EXTENSION;
        }
        return fileName;
    }

    static void prepareOutputDir(File outputDir) {
        Utils.deleteDir(outputDir);
        Utils.createDirIfAbsent(outputDir);
    }

    private static File getFileTargetDirectory(Map<String, Object> dataModel, File outputDir) {
        File targetDir;
        Object packageName = dataModel.get(DataModelFields.PACKAGE);
        if (packageName != null && Utils.isNotBlank(packageName.toString())) {
            targetDir = new File(outputDir, packageName.toString().replace(".", File.separator));
        } else {
            targetDir = outputDir;
        }
        Utils.createDirIfAbsent(targetDir);
        return targetDir;
    }

}
