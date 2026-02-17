package Domain.PatternCheck;

import Domain.Interfaces.IPatternCheck;
import Domain.LintResult;
import Domain.Category;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This class checks for the Three Layer Pattern in a given class node.
 */

public class ThreeLayerPatternCheck implements IPatternCheck {
    /**
     * Enum representing the three layers of architecture.
     */
    private Map<String, ClassInfo> analyzedClasses = new HashMap<>();
    private List<LintResult> results = new ArrayList<>();

    /**
     * Finds the classes directory (e.g., target/classes or target/test-classes)
     * that contains the provided ClassNode's .class file.
     */
    private String findClassesDirectory(ClassNode classNode) {
        String resourcePath = classNode.name + ".class"; // classNode.name uses slashes
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resource = loader.getResource(resourcePath);
        if (resource == null) {
            resource = ClassLoader.getSystemResource(resourcePath);
        }
        if (resource == null) {
            return null;
        }

        String url = resource.toString();
        try {
            if (url.startsWith("file:")) {
                Path classFilePath = Paths.get(resource.toURI());
                Path current = classFilePath.getParent();
                while (current != null) {
                    String name = current.getFileName() == null ? "" : current.getFileName().toString();
                    if ("classes".equals(name) || "test-classes".equals(name)) {
                        return current.toString();
                    }
                    current = current.getParent();
                }
                // Fallback to the directory containing the .class file
                return classFilePath.getParent().toString();
            } else if (url.startsWith("jar:")) {
                int ex = url.indexOf("!/");
                if (ex > 0) {
                    String jarUri = url.substring(4, ex); // strip leading "jar:"
                    Path jarPath = Paths.get(new URL(jarUri).toURI());
                    return jarPath.getParent().toString();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    @Override
    public List<LintResult> execute(ClassNode classNode) {
        String className = classNode.name.replace('/', '.');

        System.out.println("Starting analysis from main class: " + className);

        // Derive the classes directory from the ClassNode
        String classesDirectory = findClassesDirectory(classNode);

        if (classesDirectory == null) {
            try {
                throw new IOException("Could not determine classes directory for: " + className);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path classesPath = Paths.get(classesDirectory);

        if (!Files.exists(classesPath)) {
            try {
                throw new IOException("Classes directory not found: " + classesDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Analyzing classes in: " + classesDirectory);
        System.out.println("=".repeat(80));

        // First, analyze the main class from the provided ClassNode
        analyzeClassNode(classNode);

        // Then find and analyze all other .class files
        try (Stream<Path> paths = Files.walk(classesPath)) {
            List<Path> classFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .collect(Collectors.toList());

            System.out.println("Found " + classFiles.size() + " class files\n");

            for (Path classFile : classFiles) {
                analyzeClass(classFile);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<Reference> references = getReferences(classNode);

        evaluatePattern(references);

        return results;
    }

    private void evaluatePattern(List<Reference> references) {
        // Check each reference for three-layer pattern violations
        for (Reference ref : references) {
            String violation = checkLayerViolation(ref.sourceClass, ref.targetClass);
            if (violation != null) {
                LintResult result = new LintResult(
                    "ThreeLayerPattern",
                    Category.PATTERN,
                    ref.sourceClass.name + " (" + ref.source + ")",
                    violation
                );
                results.add(result);
            }
        }
    }
    
    /**
     * Checks if there is a three-layer pattern violation between two classes.
     * Returns null if no violation, or a violation description if one exists.
     */
    private String checkLayerViolation(ClassInfo sourceClass, ClassInfo targetClass) {
        Layer sourceLayer = sourceClass.layer;
        Layer targetLayer = targetClass.layer;
        
        // PRESENTATION should only reference DOMAIN or other PRESENTATION
        if (sourceLayer == Layer.PRESENTATION) {
            if (targetLayer == Layer.DATA) {
                return "Presentation layer class references Data layer class directly: " + targetClass.name;
            }
            if (targetLayer == Layer.UNKNOWN) {
                return "Presentation layer class references unknown layer class: " + targetClass.name;
            }
        }
        
        // DATA should not reference PRESENTATION or DOMAIN
        if (sourceLayer == Layer.DATA) {
            if (targetLayer == Layer.PRESENTATION) {
                return "Data layer class references Presentation layer class: " + targetClass.name;
            }
            if (targetLayer == Layer.DOMAIN) {
                return "Data layer class references Domain layer class: " + targetClass.name;
            }
        }
        
        // DOMAIN can reference DATA and DOMAIN, but not PRESENTATION
        if (sourceLayer == Layer.DOMAIN) {
            if (targetLayer == Layer.PRESENTATION) {
                return "Domain layer class references Presentation layer class: " + targetClass.name;
            }
        }
        
        return null;
    }
    private List<Reference> getReferences(ClassNode classNode) {
        List<Reference> refList = new ArrayList<>();
        ClassInfo classInfo = analyzedClasses.get(classNode.name.replace('/', '.'));
        // For each analyzed class, find and link its referenced classes
            if (classNode != null && classNode.fields != null) {
                for (Object fieldObj : classNode.fields) {
                    org.objectweb.asm.tree.FieldNode field = (org.objectweb.asm.tree.FieldNode) fieldObj;
                    String fieldType = extractClassNameFromDescriptor(field.desc);
                    if (fieldType != null) {
                        ClassInfo referencedClass = analyzedClasses.get(fieldType);
                        if (referencedClass != null) {
                            // Record this specific field reference
                            refList.add(new Reference(classInfo, referencedClass, "field: " + field.name));
                        }
                    }
                }
            }

            if (classNode != null) {
                for(MethodNode method : classInfo.methods){
                    if(method.instructions != null) {
                        for (Object insnObj : method.instructions) {
                            if (insnObj instanceof org.objectweb.asm.tree.MethodInsnNode) {
                                org.objectweb.asm.tree.MethodInsnNode methodCall = (org.objectweb.asm.tree.MethodInsnNode) insnObj;
                                String calledClass = methodCall.owner.replace('/', '.');
                                ClassInfo referencedClass = analyzedClasses.get(calledClass);
                                if (referencedClass != null) {
                                    // Record this specific method call reference
                                    refList.add(new Reference(classInfo, referencedClass, "method " + method.name + " calls: " + methodCall.name));
                                }
                            }
                        }
                    }
                }
            }
        
        return refList;
    }

    private void analyzeClass(Path classFile) {
        try {
            byte[] classData = Files.readAllBytes(classFile);
            ClassReader reader = new ClassReader(classData);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            analyzeClassNode(classNode);
        } catch (IOException e) {
            System.err.println("Failed to analyze class file: " + classFile);
            e.printStackTrace();
        }
    }

    private void analyzeClassNode(ClassNode classNode) {
        String className = classNode.name.replace('/', '.');
        ClassInfo classInfo = new ClassInfo(className, determineLayer(className, classNode));
        classInfo.node = classNode;
        
        // Extract superclass
        if (classNode.superName != null && !classNode.superName.equals("java/lang/Object")) {
            classInfo.superClasses.add(classNode.superName.replace('/', '.'));
        }
        
        // Extract interfaces
        if (classNode.interfaces != null) {
            for (Object iface : classNode.interfaces) {
                classInfo.superClasses.add(((String) iface).replace('/', '.'));
            }
        }

        if(classNode.methods != null) {
            for (Object methodObj : classNode.methods) {
                MethodNode method = (MethodNode) methodObj;
                classInfo.methods.add(method);
            }
        }
        
        // Store in the map
        analyzedClasses.put(className, classInfo);
    }
    
    /**
     * Converts an ASM type descriptor to a class name.
     * Examples: "Ljava/lang/String;" -> "java.lang.String", "I" -> null
     */
    private String extractClassNameFromDescriptor(String descriptor) {
        if (descriptor == null || descriptor.isEmpty()) {
            return null;
        }
        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
            return descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
        }
        // Primitive types (I, Z, V, etc.) return null
        return null;
    }
    
    /**
     * Determines which layer a class belongs to based on package name and class name.
     * The three layers are: Presentation, Business Logic (Service), and Data Access (Persistence).
     */
    private Layer determineLayer(String className, ClassNode classNode) {
        String lowerClassName = className.toLowerCase();
        String lowerPackage = className.substring(0, Math.max(0, className.lastIndexOf('.'))).toLowerCase();
        
        // Check for Presentation layer indicators
        if (lowerPackage.contains("presentation") || lowerPackage.contains("controller") || 
            lowerPackage.contains("view") || lowerPackage.contains("ui") ||
            lowerClassName.contains("controller") || lowerClassName.contains("view") || 
            lowerClassName.contains("frame") || lowerClassName.contains("panel")) {
            return Layer.PRESENTATION;
        }
        
        // Check for Data Access layer indicators
        if (lowerPackage.contains("persistence") || lowerPackage.contains("repository") || 
            lowerPackage.contains("dao") || lowerPackage.contains("data") ||
            lowerClassName.contains("repository") || lowerClassName.contains("dao") || 
            lowerClassName.contains("mapper") || lowerClassName.contains("query")) {
            return Layer.DATA;
        }
        
        // Check for Business Logic layer indicators
        if (lowerPackage.contains("service") || lowerPackage.contains("business") || 
            lowerPackage.contains("logic") || lowerPackage.contains("domain") ||
            lowerClassName.contains("service") || lowerClassName.contains("manager") || 
            lowerClassName.contains("processor") || lowerClassName.contains("handler")) {
            return Layer.DOMAIN;
        }
        
        // Default to unknown if no indicators found
        return Layer.UNKNOWN;
    }
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "ThreeLayerPattern";
    }

    private class ClassInfo {
        String name;
        Layer layer;
        ClassNode node;
        List<String> superClasses = new ArrayList<>();
        List<MethodNode> methods = new ArrayList<>();
        List<ClassInfo> vars = new ArrayList<>();

        public ClassInfo(String name, Layer layer) {
            this.name = name;
            this.layer = layer;
        }

        public void addVar(ClassInfo var) {
            vars.add(var);
        }
    }

    enum Layer {
        PRESENTATION,
        DOMAIN,
        DATA,
        UNKNOWN
    }
    
    /**
     * Inner class to track individual references between classes.
     * Records which class references which other class and the source (field/method).
     */
    private class Reference {
        ClassInfo sourceClass;
        ClassInfo targetClass;
        String source;  // e.g., "field: userList" or "method doCalculation calls: getValue"
        
        Reference(ClassInfo sourceClass, ClassInfo targetClass, String source) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
            this.source = source;
        }
    }
}


