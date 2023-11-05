package fpt.edu.capstone.vms.keycloak.sync.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.reflect.ClassPath;
import fpt.edu.capstone.vms.keycloak.sync.adapter.KeycloakRolePathAdapter;
import fpt.edu.capstone.vms.keycloak.sync.mapper.KeycloakRoleMapper;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleAttribute;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleConstraint;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleExtractConfig;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleExtractResults;
import fpt.edu.capstone.vms.util.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public final class KeycloakRoleExtractorHelper {

    private static final Logger log = LoggerFactory.getLogger(KeycloakRoleExtractorHelper.class);

    public static KeycloakRoleExtractResults extract(KeycloakRoleExtractConfig args) throws IOException {
        return execute(args, null);
    }

    public static KeycloakRoleExtractResults extract(KeycloakRoleExtractConfig args, String fileName) throws IOException {
        return execute(args, fileName);
    }

    public static void extract2File(KeycloakRoleExtractConfig args, String fileName) throws IOException {
        var apiConfig = execute(args, fileName);

        // Extract JSON
        writingJsonFile(apiConfig, fileName);
    }

    public static KeycloakRolePathAdapter adaptRolesPath(File data, String module) throws IOException {
        List<String> rolePaths = JacksonUtils.getList(Files.readString(data.toPath()), String.class);
        return new KeycloakRolePathAdapter(rolePaths, module);
    }

    public static KeycloakRolePathAdapter adaptRolePaths(List<String> roles, String module) {
        return new KeycloakRolePathAdapter(roles, module);
    }

    public static void adaptRolePaths2File(File data, String module, String fileName) throws IOException {
        // Extract JSON
        writingJsonFile(adaptRolesPath(data, module), fileName);
    }

    public static void adaptRolePaths2File(List<String> roles, String module, String fileName) throws IOException {
        // Extract JSON
        writingJsonFile(adaptRolePaths(roles, module), fileName);
    }

    private static KeycloakRoleExtractResults execute(KeycloakRoleExtractConfig args, String fileName) throws IOException {
        KeycloakRoleExtractResults oldKeycloakRoleExtractResults = fileName != null ? JacksonUtils.getObject(new File(fileName), KeycloakRoleExtractResults.class) : new KeycloakRoleExtractResults();

        ArrayList<String> roleStrings = new ArrayList<>();
        List<KeycloakRoleConstraint> constraints = new ArrayList<>();
        KeycloakRoleExtractResults keycloakRoleExtractResults = new KeycloakRoleExtractResults();
        Map<String, Map<String, KeycloakRoleAttribute>> roles = new HashMap<>();
        Map<String, Map<String, KeycloakRoleAttribute>> oldRoles = args.getCleanup().equals("false") ? oldKeycloakRoleExtractResults.getRoles() : new HashMap<>();

        findAllClassWith(args.getStartWith(), args.getEndWith()).forEach(clazz -> {
            RequestMapping requestMapping = AnnotationUtils.getAnnotation(clazz, RequestMapping.class);
            if (requestMapping == null) return;
            String classPath = requestMapping.path()[0];
            final Method[] methodInClass = clazz.getDeclaredMethods();

            Arrays.stream(methodInClass).forEach(myMethod -> {
                /* Insert role string */
                addRolesStr(myMethod, roleStrings);

                /* Insert constraint */
                addConstraints(myMethod, classPath, constraints);
            });
        });

        /* Convert role string to roles map details */
        roleStrings.forEach(role -> {
            roles.put(role, KeycloakRoleMapper.convert2RoleMap(role, oldRoles));
        });

        keycloakRoleExtractResults.setRoles(roles);

        log.info("-------------------------------------------");
        log.info("|     Success extract keycloak config      |");
        log.info("|     Roles {} role(s)                     |", keycloakRoleExtractResults.getRoles().size());
        log.info("-------------------------------------------");

        return keycloakRoleExtractResults;
    }

    private static void addRolesStr(Method method, ArrayList<String> rolesStrings) {
        final PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        if (Objects.nonNull(preAuthorize)) {
            String[] rolesPreAuthorize = new String[]{preAuthorize.value()};
            String[] fullMultiplyRole = new String[rolesPreAuthorize.length];

            /* Format roles */
            for (int i = 0; i < rolesPreAuthorize.length; i++) {
                fullMultiplyRole[i] = formatRole(rolesPreAuthorize[i]);

                if (!fullMultiplyRole[i].contains(",")) {
                    rolesStrings.add(fullMultiplyRole[i].trim());
                } else {
                    String[] elementRoles = fullMultiplyRole[i].split(",");
                    List<String> _elementRoles = Arrays.stream(elementRoles).map(String::trim).toList();
                    rolesStrings.addAll(_elementRoles);
                }
            }
        }
    }

    private static void addConstraints(Method method, String classPath, List<KeycloakRoleConstraint> constraints) {
        final RequestMapping merged = AnnotatedElementUtils.getMergedAnnotation(method, RequestMapping.class);
        final PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        KeycloakRoleConstraint constraint = new KeycloakRoleConstraint();
        constraint.setRoles(Objects.isNull(preAuthorize) ? new String[]{} : arrayRoles(preAuthorize.value()));
        constraint.setMethod(Objects.isNull(merged) ? null : merged.method()[0].name());
        constraint.setPath(Objects.isNull(merged) || merged.path().length == 0 ? "" : classPath + merged.path()[0]);
        constraint.setScopes(new String[]{});

        constraints.add(constraint);
    }

    private static Set<Class> findAllClassWith(String startWith, String endWith) throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream().filter(item -> item.getName().toLowerCase().startsWith(startWith)).filter(item -> item.getName().toLowerCase().endsWith(endWith)).map(ClassPath.ClassInfo::load).collect(Collectors.toSet());
    }

    private static String[] oneRoles(String roles) {
        String[] result = new String[1];
        result[0] = formatRole(roles);
        return result;
    }

    private static String[] multipleRoles(String roles) {
        String rolesFormat = formatRole(roles);
        return rolesFormat.split(",");
    }

    // Get all roles in 1 annotation @PreAuthorize
    private static String[] arrayRoles(String roles) {
        if (roles.startsWith("hasRole")) {
            return oneRoles(roles);
        } else {
            return multipleRoles(roles);
        }
    }

    private static String formatOneRoles(String roles) {
        return roles.replace("hasRole('", "").replace("')", "");
    }

    private static String formatMultipleRoles(String roles) {
        String formattedRoles = roles.replace("hasAnyRole('", "").replace("')", "");
        formattedRoles = formattedRoles.replace("'", "");
        return formattedRoles;
    }

    private static String formatRole(String roles) {
        if (roles.startsWith("hasRole")) {
            return formatOneRoles(roles);
        } else {
            return formatMultipleRoles(roles);
        }
    }

    /* EXTRACT JSON */
    private static void writingJsonFile(KeycloakRoleExtractResults keycloakRoleExtractResults, String fileName) throws IOException {
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

        // Write keycloak permissions
        try {
            writer.writeValue(new File(fileName), keycloakRoleExtractResults);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }

        // Write web application roles
        try {
            Map<String, List<String>> map = keycloakRoleExtractResults.generateRoleMapForWeb();
            if (null == map || map.isEmpty()) return;
            String webRoleFileStr = fileName.replace("permission-setting.json", "web-roles.json");
            writer.writeValue(new File(webRoleFileStr), map);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /* EXTRACT CSV */
    public static void writingCsvFile(String fileName, List<KeycloakRoleConstraint> keycloakRoleConstraints) throws IOException {
        if (!fileName.endsWith(".csv")) {
            throw new FileNotFoundException("Can't find file");
        }

        try {
            FileWriter csvWriter = new FileWriter(fileName);

            /* Header */
            csvWriter.append("PATH");
            csvWriter.append(",");
            csvWriter.append("METHOD");
            csvWriter.append(",");
            csvWriter.append("ROLES");
            csvWriter.append("\n");

            /* Content */
            for (KeycloakRoleConstraint keycloakRoleConstraint : keycloakRoleConstraints) {
                csvWriter.append(keycloakRoleConstraint.getPath());
                csvWriter.append(",");
                csvWriter.append(keycloakRoleConstraint.getMethod());
                csvWriter.append(",");

                String[] roles = keycloakRoleConstraint.getRoles();
                for (int i = 0; i < roles.length; i++) {
                    csvWriter.append(roles[i]);
                    if (i != roles.length - 1) {
                        csvWriter.append(" | ");
                    }
                }
                csvWriter.append("\n");
            }

            csvWriter.close();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
}
