package fpt.edu.capstone.vms.oauth2;


import fpt.edu.capstone.vms.controller.IPermissionController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.RoleAttributes;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public interface IPermissionResource {

    /**
     * Find all module info (Keycloak: modules is client, permission is Client role)
     *
     * @param fetchPermission true to eager load, false to lazy load.
     * @return List<ModuleDto>
     */
    List<ModuleDto> findAllModules(boolean fetchPermission);

    /**
     * Find all role in modules (Keycloak: modules is client, permission is Client role)
     *
     * @param clientId variable identify for modules (Keycloak: clientId is clientUUID)
     * @return List<PermissionDto>
     */
    List<PermissionDto> findAllByModuleId(String clientId);
    List<PermissionDto> findAllOrgByModuleId(String clientId);

    /**
     * Find permission by id (Keycloak: modules is client, permission is Client role)
     *
     * @param clientId     variable identify for modules (Keycloak: clientId is clientUUID).
     * @param permissionId variable identify for permission (Keycloak: permissionId is Client roleName).
     * @return PermissionDto
     */
    PermissionDto findById(String clientId, String permissionId);

    /**
     * Create Permission (Keycloak: modules is client, permission is Client role)
     *
     * @param clientId variable identify for modules (Keycloak: clientId is clientUUID).
     * @param dto      DTO for permission object
     */
    PermissionDto create(String clientId, PermissionDto dto);

    /**
     * Update Permission (Keycloak: modules is client, permission is Client role)
     *
     * @param clientId     variable identify for modules (Keycloak: clientId is clientUUID).
     * @param permissionId variable identify for permission (Keycloak: permissionId is Client roleName).
     * @param dto          DTO for permission object
     * @throws NotFoundException if not exists
     */
    PermissionDto update(String clientId, String permissionId, PermissionDto dto) throws NotFoundException;

    /**
     * Update role (Keycloak: role is realm role)
     *
     * @param clientId       variable identify for modules (Keycloak: clientId is clientUUID).
     * @param attributes     attributes for permission.
     * @param permissionDtos permission data transfer object to update.
     */
    void updateAttribute(String clientId, Map<String, List<String>> attributes, List<PermissionDto> permissionDtos);

    /**
     * Delete Permission (Keycloak: modules is client, permission is Client role)
     *
     * @param clientId     variable identify for modules (Keycloak: clientId is clientUUID).
     * @param permissionId variable identify for permission (Keycloak: permissionId is Client roleName).
     */
    void delete(String clientId, String permissionId);

    List filter(IPermissionController.PermissionFilterPayload filterPayload);

    @Data
    @Accessors(chain = true)
    class ModuleDto {
        private String id;
        private String clientId;
        private String name;
        private List<PermissionDto> permissionDtos;
    }

    @Data
    @Accessors(chain = true)
    class PermissionDto {
        private String id;
        private String name;
        private String clientId;
        private String group;
        private Map<String, List<String>> attributes;
        private Map<String, Map<String, String>> label = new HashMap<>();

        public PermissionDto initMetadata() {
            this.setGroup(attributes.get(RoleAttributes.GROUP.getValue()).get(0));

            var label = this.getAttributes()
                .entrySet().stream()
                .filter((attribute) -> attribute.getKey().contains(":"))
                .collect(groupingBy((entry) -> entry.getKey().split(":")[1]))
                .entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    (entry) -> entry.getValue().stream()
                        .collect(Collectors.toMap(
                            (attribute) -> attribute.getKey().split(":")[0],
                            (attribute) -> attribute.getValue().get(0))
                        )
                ));
            this.setLabel(label);
            return this;
        }
    }

}
