/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.intellij.runner.functions.core;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.lang.jvm.JvmAnnotation;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteral;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.microsoft.azure.common.exceptions.AzureExecutionException;
import com.microsoft.azure.common.function.bindings.Binding;
import com.microsoft.azure.common.function.bindings.BindingEnum;
import com.microsoft.azure.common.function.configurations.FunctionConfiguration;
import com.microsoft.azure.common.function.utils.CommandUtils;
import com.microsoft.azure.functions.annotation.StorageAccount;
import com.microsoft.azure.maven.common.utils.SneakyThrowUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FunctionUtils {

    private static final String FUNCTION_JSON = "function.json";
    public static final String FUNCTION_JAVA_LIBRARY_ARTIFACT_ID = "azure-functions-java-library";
    private static final String AZURE_FUNCTION_ANNOTATION_CLASS = "com.microsoft.azure.functions.annotation.FunctionName";
    private static final String HTTP_OUTPUT_DEFAULT_NAME = "$return";
    private static final String DEFAULT_HOST_JSON = "{\"version\":\"2.0\",\"extensionBundle\":{\"id\":\"Microsoft.Azure.Functions.ExtensionBundle\",\"version\":\"[1.*, 2.0.0)\"}}\n";

    public static Module[] listFunctionModules(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        return Arrays.stream(modules).filter(m -> {
            final GlobalSearchScope scope = GlobalSearchScope.moduleWithLibrariesScope(m);
            PsiClass ecClass = JavaPsiFacade.getInstance(project).findClass(AZURE_FUNCTION_ANNOTATION_CLASS, scope);
            return ecClass != null;
        }).toArray(Module[]::new);
    }

    public static Module getFunctionModuleByFilePath(Project project, String moduleFilePath){
        Module[] modules = listFunctionModules(project);
        return Arrays.stream(modules)
                .filter(module -> StringUtils.equals(moduleFilePath, module.getModuleFilePath()))
                .findFirst().orElse(null);
    }

    public static boolean isFunctionProject(Project project) {
        List<Library> libraries = new ArrayList<>();
        OrderEnumerator.orderEntries(project).forEachLibrary(library -> libraries.add(library));
        return ListUtils.indexOf(libraries,
                library -> StringUtils.contains(library.getName(), FUNCTION_JAVA_LIBRARY_ARTIFACT_ID)) >= 0;
    }

    public static PsiMethod[] findFunctionsByAnnotation(Module module) {
        Project project = module.getProject();

        PsiClass functionNameClass = JavaPsiFacade.getInstance(module.getProject())
                .findClass(AZURE_FUNCTION_ANNOTATION_CLASS, GlobalSearchScope.moduleWithLibrariesScope(module));
        List<PsiMethod> methods = new ArrayList<>(AnnotatedElementsSearch
                .searchPsiMethods(functionNameClass, GlobalSearchScope.allScope(project)).findAll());
        return methods.toArray(new PsiMethod[0]);
    }

    public static final Path getDefaultHostJson(Project project) {
        File projectHost = new File(project.getBasePath(), "host.json");
        return projectHost.exists() ? projectHost.toPath() : createTempleHostJson();
    }

    public static final Path createTempleHostJson() {
        try {
            File result = File.createTempFile("host", ".json");
            FileUtils.write(result, DEFAULT_HOST_JSON, Charset.defaultCharset());
            return result.toPath();
        } catch (IOException e) {
            return null;
        }
    }

    public static void prepareStagingFolderForDeploy(Path stagingFolder, Path hostJson, Module module, PsiMethod[] methods)
            throws AzureExecutionException, IOException {
        prepareStagingFolder(stagingFolder, hostJson, null, module, methods);
    }


    public static void prepareStagingFolder(Path stagingFolder, Path hostJson, Path localSettingJson, Module module,
            PsiMethod[] methods) throws AzureExecutionException, IOException {
        Map<String, FunctionConfiguration> configMap = generateConfigurations(methods);
        final Path jarFile = JarUtils.buildJarFileToStagingPath(stagingFolder.toString(), module);
        final String scriptFilePath = "../" + jarFile.getFileName().toString();
        configMap.values().forEach(config -> config.setScriptFile(scriptFilePath));
        for (final Map.Entry<String, FunctionConfiguration> config : configMap.entrySet()) {
            if (StringUtils.isNotBlank(config.getKey())) {
                final File functionJsonFile = Paths.get(stagingFolder.toString(), config.getKey(), FUNCTION_JSON)
                        .toFile();
                FunctionJsonWriter.writeFunctionJsonFile(functionJsonFile, config.getValue());
            }
        }
        FileUtils.copyFile(hostJson.toFile(), new File(stagingFolder.toFile(),"host.json"));
        if (localSettingJson != null) {
            FileUtils.copyFile(localSettingJson.toFile(), new File(stagingFolder.toFile(), "local.settings.json"));
        }

        final List<File> jarFiles = new ArrayList<>();
        OrderEnumerator.orderEntries(module).productionOnly().forEachLibrary(lib -> {
            if (StringUtils.contains(lib.getName(), FUNCTION_JAVA_LIBRARY_ARTIFACT_ID)) {
                return true;
            }

            if (lib != null) {
                for (VirtualFile virtualFile : lib.getFiles(OrderRootType.CLASSES)) {
                    final File file = new File(stripExtraCharacters(virtualFile.getPath()));
                    if (file.exists()) {
                        jarFiles.add(file);
                    }
                }
            }
            return true;
        });

        for (File file : jarFiles) {
            FileUtils.copyFileToDirectory(file, new File(stagingFolder.toFile(), "lib"));
        }
    }

    private static String stripExtraCharacters(String fileName) {
        // TODO-dp this is not robust enough (eliminated !/ at the end of the jar)
        if (fileName.endsWith("!/")) {
            fileName = fileName.substring(0, fileName.length() - 2);
        }
        return fileName;
    }

    public static Map<String, FunctionConfiguration> generateConfigurations(final PsiMethod[] methods)
            throws AzureExecutionException {
        final Map<String, FunctionConfiguration> configMap = new HashMap<>();
        for (final PsiMethod method : methods) {
            PsiAnnotation annotation = AnnotationUtil.findAnnotation(method,
                    FunctionUtils.AZURE_FUNCTION_ANNOTATION_CLASS);
            final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
            String functionName = null;
            for (PsiNameValuePair attribute : attributes) {
                final PsiAnnotationMemberValue value = attribute.getValue();
                String name = attribute.getAttributeName();
                if ("value".equals(name)) {
                    functionName = getAnnotationValueAsString(value);
                    break;
                }
            }
            configMap.put(functionName, generateConfiguration(method));
        }
        return configMap;
    }

    private static FunctionConfiguration generateConfiguration(PsiMethod method) throws AzureExecutionException {
        final FunctionConfiguration config = new FunctionConfiguration();
        List<Binding> bindings = new ArrayList<>();
        processParameterAnnotations(method, bindings);
        processMethodAnnotations(method, bindings);
        patchStorageBinding(method, bindings);
        config.setEntryPoint(method.getContainingClass().getQualifiedName() + "." + method.getName());
        // Todo: add set bindings method in tools-common
        //config.setBindings(bindings);
        return config;
    }

    private static void processParameterAnnotations(final PsiMethod method, final List<Binding> bindings)
            throws AzureExecutionException {
        for (final JvmParameter param : method.getParameters()) {
            bindings.addAll(parseAnnotations(param.getAnnotations()));
        }
    }

    private static List<Binding> parseAnnotations(JvmAnnotation[] annos) throws AzureExecutionException {
        final List<Binding> bindings = new ArrayList<>();

        for (final JvmAnnotation annotation : annos) {
            final Binding binding = getBinding(annotation);
            if (binding != null) {
                System.out.println("Adding binding: " + binding.toString());
                bindings.add(binding);
            }
        }

        return bindings;
    }

    private static Binding getBinding(JvmAnnotation annotation) throws AzureExecutionException {
        BindingEnum annotationEnum = Arrays.stream(BindingEnum.values()).filter((bindingEnum) -> {
            return bindingEnum.name().toLowerCase(Locale.ENGLISH)
                    .equals(FilenameUtils.getExtension(annotation.getQualifiedName()).toLowerCase(Locale.ENGLISH));
        }).findFirst().orElse(null);
        return annotationEnum == null ? getUserDefinedBinding(annotation)
                : createBinding(annotationEnum, (PsiAnnotation) annotation);
    }

    private static Binding getUserDefinedBinding(JvmAnnotation annotation) {
        // unsupported now
        return null;
    }

    private static void processMethodAnnotations(final PsiMethod method, final List<Binding> bindings)
            throws AzureExecutionException {
        if (!method.getReturnType().equals(Void.TYPE)) {
            bindings.addAll(parseAnnotations(method.getAnnotations()));

            if (bindings.stream().anyMatch(b -> b.getBindingEnum() == BindingEnum.HttpTrigger)
                    && bindings.stream().noneMatch(b -> StringUtils.equalsIgnoreCase(b.getName(), "$return"))) {
                bindings.add(getHTTPOutBinding());
            }
        }
    }

    private static Binding getHTTPOutBinding() {
        final Binding result = new Binding(BindingEnum.HttpOutput);
        result.setName(HTTP_OUTPUT_DEFAULT_NAME);
        return result;
    }

    private static void patchStorageBinding(final PsiMethod method, final List<Binding> bindings) {
        final PsiAnnotation storageAccount = AnnotationUtil.findAnnotation(method,
                StorageAccount.class.getCanonicalName());

        if (storageAccount != null) {
            System.out.println("StorageAccount annotation found.");
            storageAccount.getAttributes().forEach(t -> {
                if (t.getAttributeName().equals("value")) {
                    try {
                        final String connectionString = getAnnotationValueAsString2(t.getAttributeValue());
                        bindings.stream().filter(binding -> binding.getBindingEnum().isStorage())
                                .filter(binding -> StringUtils.isEmpty((String) binding.getAttribute("connection")))
                                .forEach(binding -> binding.setAttribute("connection", connectionString));
                    } catch (AzureExecutionException e) {
                        SneakyThrowUtils.sneakyThrow(e);
                    }

                }
            });
            // Replace empty connection string

        } else {
            System.out.println("No StorageAccount annotation found.");
        }
    }

    public static String getAnnotationValueAsString2(JvmAnnotationAttributeValue value) throws AzureExecutionException {
        if (value instanceof JvmAnnotationConstantValue) {
            return Objects.toString(((JvmAnnotationConstantValue) value).getConstantValue(), null);
        }
        if (value instanceof PsiExpression) {
            if (value instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression) value;
                Object resolved = referenceExpression.resolve();
                if (resolved instanceof PsiEnumConstant) {
                    final PsiEnumConstant enumConstant = (PsiEnumConstant) resolved;
                    final PsiClass enumClass = enumConstant.getContainingClass();
                    if (enumClass != null) {
                        try {
                            return getEnumFieldString(enumClass.getQualifiedName(), enumConstant.getName());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return enumConstant.getName();
                    }
                }

            }
            Object obj = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression) value, true);
            return Objects.toString(obj, null);
        } else if (value instanceof PsiLiteral) {
            return Objects.toString(((PsiLiteral) value).getValue(), null);
        }
        throw new AzureExecutionException("Cannot get annotation value of type : " + value.getClass().getName());
    }

    public static String getAnnotationValueAsString(PsiAnnotationMemberValue value) throws AzureExecutionException {
        if (value instanceof PsiExpression) {
            if (value instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression) value;
                Object resolved = referenceExpression.resolve();
                if (resolved instanceof PsiEnumConstant) {
                    final PsiEnumConstant enumConstant = (PsiEnumConstant) resolved;
                    final PsiClass enumClass = enumConstant.getContainingClass();
                    if (enumClass != null) {
                        try {
                            return getEnumFieldString(enumClass.getQualifiedName(), enumConstant.getName());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return enumConstant.getName();
                    }
                }

            }
            Object obj = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression) value, true);
            return Objects.toString(obj, null);
        } else if (value instanceof PsiLiteral) {
            return Objects.toString(((PsiLiteral) value).getValue(), null);
        }
        throw new AzureExecutionException("Cannot get annotation value of type : " + value.getClass().getName());
    }

    public static String getTargetFolder(Module module) {
        if (module == null) {
            return StringUtils.EMPTY;
        }
        final Project project = module.getProject();
        final MavenProject mavenProject = MavenProjectsManager.getInstance(project).findProject(module);
        final String stagingFolderName = mavenProject == null ? project.getName() : mavenProject.getProperties().getProperty("functionAppName");
        return Paths.get(project.getBasePath(), "target", "azure-functions", stagingFolderName).toString();
    }

    public static String getFuncPath() throws IOException, InterruptedException {
        final List<String> outputStrings = executeMultipLineOutput(
                CommandUtils.isWindows() ? "where func" : "which func");
        for (String outputLine : outputStrings) {
            if (StringUtils.isBlank(outputLine)) {
                continue;
            }
            final File file = new File(outputLine.replaceAll("\\r|\\n", ""));
            if (file.exists() && file.isFile()) {
                File funcExe = new File(
                        Paths.get(file.getParent(), "node_modules", "azure-functions-core-tools", "bin").toFile(),
                        CommandUtils.isWindows() ? "func.exe" : "func");
                if (funcExe.exists()) {
                    return funcExe.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static List<String> executeMultipLineOutput(String cmd) throws IOException, InterruptedException {
        final List<String> result = new ArrayList<>();
        final Process process = Runtime.getRuntime().exec(cmd);
        StringBuffer ret = new StringBuffer();
        try (final InputStream inputStream = process.getInputStream();
             final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             final BufferedReader br = new BufferedReader(inputStreamReader)) {
            String tmp;
            while ((tmp = br.readLine()) != null) {
                result.add(tmp);
            }
        }
        if (process.waitFor() != 0) {
            throw new IOException("Command execute fail.");
        }
        return result;
    }

    private static Binding createBinding(BindingEnum bindingEnum, PsiAnnotation annotation) throws AzureExecutionException {
        Binding binding = new Binding(bindingEnum);
        final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
        for (PsiNameValuePair attribute : attributes) {
            final PsiAnnotationMemberValue value = attribute.getValue();
            String name = attribute.getAttributeName();
            if (value instanceof PsiArrayInitializerMemberValue) {
                PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) value)
                        .getInitializers();
                List<String> result = Lists.newArrayListWithCapacity(initializers.length);

                for (PsiAnnotationMemberValue initializer : initializers) {
                    result.add(getAnnotationValueAsString(initializer));
                }
                binding.setAttribute(name, result.toArray(new String[0]));
            } else {
                String valueText = getAnnotationValueAsString(value);
                binding.setAttribute(name, valueText);
            }
        }
        return binding;
    }

    public static String getEnumFieldString(final String className, final String fieldName)
            throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        final Class<?> c = Class.forName(className);
        final Field[] a = c.getFields();
        final Optional<Field> targetField = Arrays.stream(a).filter(t -> t.getName().equals(fieldName)).findFirst();
        if (targetField.isPresent()) {
            return Objects.toString(targetField.get().get(null));
        }
        return null;
    }

}
