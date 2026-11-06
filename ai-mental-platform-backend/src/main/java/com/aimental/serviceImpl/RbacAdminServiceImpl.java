package com.aimental.serviceImpl;

import com.aimental.dto.PermissionSaveDTO;
import com.aimental.dto.RolePermAssignDTO;
import com.aimental.dto.RoleSaveDTO;
import com.aimental.entity.SysPermission;
import com.aimental.entity.SysRole;
import com.aimental.entity.SysRolePermission;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.SysPermissionMapper;
import com.aimental.mapper.SysRoleMapper;
import com.aimental.mapper.SysRolePermissionMapper;
import com.aimental.service.RbacAdminService;
import com.aimental.vo.PermissionVO;
import com.aimental.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RBAC权限管理服务实现类
 *
 * 角色维护：新增/编辑/删除/查询角色；
 * 权限维护：新增/编辑/删除/查询权限字典；
 * 角色权限分配：全量覆盖式分配（以请求permIds为准），删除角色/权限时级联清理关联关系
 */
@Service
public class RbacAdminServiceImpl implements RbacAdminService {

    private static final Logger log = LoggerFactory.getLogger(RbacAdminServiceImpl.class);

    /**
     * 系统角色Mapper
     */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 系统权限Mapper
     */
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 角色权限关联Mapper
     */
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /**
     * 构造方法注入
     *
     * @param sysRoleMapper          系统角色Mapper
     * @param sysPermissionMapper    系统权限Mapper
     * @param sysRolePermissionMapper 角色权限关联Mapper
     */
    public RbacAdminServiceImpl(SysRoleMapper sysRoleMapper,
                                SysPermissionMapper sysPermissionMapper,
                                SysRolePermissionMapper sysRolePermissionMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
    }

    @Override
    public List<RoleVO> getRoles() {
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getRoleId));
        return roles.stream().map(RoleVO::of).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(RoleSaveDTO dto) {
        // 1. 参数校验
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNotBlank(dto.getRoleCode(), "角色编码不能为空");
        checkMaxLength(dto.getRoleCode(), 50, "角色编码不能超过50个字符");
        checkNotBlank(dto.getRoleName(), "角色名称不能为空");
        checkMaxLength(dto.getRoleName(), 50, "角色名称不能超过50个字符");
        if (dto.getDescription() != null) {
            checkMaxLength(dto.getDescription(), 255, "角色描述不能超过255个字符");
        }
        int status = dto.getStatus() == null ? ROLE_STATUS_ENABLE : dto.getStatus();
        if (status != ROLE_STATUS_DISABLE && status != ROLE_STATUS_ENABLE) {
            throw new IllegalArgumentException("角色状态只能为0停用或1启用");
        }

        // 2. 角色编码唯一性校验
        Long exists = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, dto.getRoleCode().trim()));
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }

        // 2.5 物理清理同编码逻辑删除残留行（唯一键被残留行占用会导致新增主键冲突）
        sysRoleMapper.physicalDeleteByRoleCode(dto.getRoleCode().trim());

        // 3. 新增角色
        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode().trim());
        role.setRoleName(dto.getRoleName().trim());
        role.setDescription(dto.getDescription());
        role.setStatus(status);
        sysRoleMapper.insert(role);
        log.info("新增角色: roleId={}, roleCode={}", role.getRoleId(), role.getRoleCode());
        return RoleVO.of(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(Long roleId, RoleSaveDTO dto) {
        // 1. 角色存在性校验
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 2. 参数校验（角色编码不允许修改，dto中的roleCode被忽略）
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNotBlank(dto.getRoleName(), "角色名称不能为空");
        checkMaxLength(dto.getRoleName(), 50, "角色名称不能超过50个字符");
        if (dto.getDescription() != null) {
            checkMaxLength(dto.getDescription(), 255, "角色描述不能超过255个字符");
        }
        if (dto.getStatus() != null) {
            if (dto.getStatus() != ROLE_STATUS_DISABLE && dto.getStatus() != ROLE_STATUS_ENABLE) {
                throw new IllegalArgumentException("角色状态只能为0停用或1启用");
            }
            role.setStatus(dto.getStatus());
        }

        // 3. 更新角色
        role.setRoleName(dto.getRoleName().trim());
        role.setDescription(dto.getDescription());
        sysRoleMapper.updateById(role);
        log.info("编辑角色: roleId={}", roleId);
        return RoleVO.of(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        // 1. 角色存在性校验
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 2. 内置角色保护
        if (BUILTIN_ROLE_CODE.equals(role.getRoleCode())) {
            throw new BusinessException(400, "内置角色不允许删除");
        }

        // 3. 级联物理清理该角色的权限分配，再删除角色
        sysRolePermissionMapper.physicalDeleteByRoleId(roleId);
        sysRoleMapper.deleteById(roleId);
        log.info("删除角色: roleId={}, roleCode={}", roleId, role.getRoleCode());
    }

    @Override
    public List<PermissionVO> getPermissions() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getPermId));
        return permissions.stream().map(PermissionVO::of).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionVO createPermission(PermissionSaveDTO dto) {
        // 1. 参数校验
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNotBlank(dto.getPermCode(), "权限编码不能为空");
        checkMaxLength(dto.getPermCode(), 100, "权限编码不能超过100个字符");
        checkNotBlank(dto.getPermName(), "权限名称不能为空");
        checkMaxLength(dto.getPermName(), 100, "权限名称不能超过100个字符");
        if (dto.getDescription() != null) {
            checkMaxLength(dto.getDescription(), 255, "权限说明不能超过255个字符");
        }
        int permType = dto.getPermType() == null ? PERM_TYPE_MENU : dto.getPermType();
        if (permType != PERM_TYPE_MENU && permType != PERM_TYPE_BUTTON) {
            throw new IllegalArgumentException("权限类型只能为1菜单或2按钮/操作");
        }

        // 2. 权限编码唯一性校验
        Long exists = sysPermissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermCode, dto.getPermCode().trim()));
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "权限编码已存在");
        }

        // 2.5 物理清理同编码逻辑删除残留行（唯一键被残留行占用会导致新增主键冲突）
        sysPermissionMapper.physicalDeleteByPermCode(dto.getPermCode().trim());

        // 3. 新增权限
        SysPermission permission = new SysPermission();
        permission.setPermCode(dto.getPermCode().trim());
        permission.setPermName(dto.getPermName().trim());
        permission.setPermType(permType);
        permission.setDescription(dto.getDescription());
        sysPermissionMapper.insert(permission);
        log.info("新增权限: permId={}, permCode={}", permission.getPermId(), permission.getPermCode());
        return PermissionVO.of(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionVO updatePermission(Long permId, PermissionSaveDTO dto) {
        // 1. 权限存在性校验
        if (permId == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        SysPermission permission = sysPermissionMapper.selectById(permId);
        if (permission == null) {
            throw new BusinessException(404, "权限不存在");
        }

        // 2. 参数校验（权限编码不允许修改，dto中的permCode被忽略）
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNotBlank(dto.getPermName(), "权限名称不能为空");
        checkMaxLength(dto.getPermName(), 100, "权限名称不能超过100个字符");
        if (dto.getDescription() != null) {
            checkMaxLength(dto.getDescription(), 255, "权限说明不能超过255个字符");
        }
        if (dto.getPermType() != null) {
            if (dto.getPermType() != PERM_TYPE_MENU && dto.getPermType() != PERM_TYPE_BUTTON) {
                throw new IllegalArgumentException("权限类型只能为1菜单或2按钮/操作");
            }
            permission.setPermType(dto.getPermType());
        }

        // 3. 更新权限
        permission.setPermName(dto.getPermName().trim());
        permission.setDescription(dto.getDescription());
        sysPermissionMapper.updateById(permission);
        log.info("编辑权限: permId={}", permId);
        return PermissionVO.of(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long permId) {
        // 1. 权限存在性校验
        if (permId == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        SysPermission permission = sysPermissionMapper.selectById(permId);
        if (permission == null) {
            throw new BusinessException(404, "权限不存在");
        }

        // 2. 级联物理清理引用该权限的角色分配，再删除权限
        sysRolePermissionMapper.physicalDeleteByPermId(permId);
        sysPermissionMapper.deleteById(permId);
        log.info("删除权限: permId={}, permCode={}", permId, permission.getPermCode());
    }

    @Override
    public List<PermissionVO> getRolePermissions(Long roleId) {
        // 1. 角色存在性校验
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 2. 查询角色关联的权限ID
        List<SysRolePermission> relations = sysRolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> permIds = relations.stream()
                .map(SysRolePermission::getPermId)
                .collect(Collectors.toList());

        // 3. 批量查询权限详情并转VO
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getPermId, permIds)
                .orderByAsc(SysPermission::getPermId));
        return permissions.stream().map(PermissionVO::of).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, RolePermAssignDTO dto) {
        // 1. 角色存在性校验
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 2. 权限ID去空、去重（null视为清空该角色全部权限）
        List<Long> permIds = dto == null || dto.getPermIds() == null
                ? new ArrayList<>()
                : dto.getPermIds().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                        .stream().collect(Collectors.toList());

        // 3. 校验全部权限ID真实存在（逻辑删除的不计入）
        if (!permIds.isEmpty()) {
            Long count = sysPermissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                    .in(SysPermission::getPermId, permIds));
            long expected = new LinkedHashSet<>(permIds).size();
            if (count == null || count.longValue() != expected) {
                throw new BusinessException(400, "存在不存在的权限ID");
            }
        }

        // 4. 全量覆盖：先物理删除旧分配（逻辑删除会占用uk_role_perm唯一键，重插同权限会主键冲突），再插入新分配
        sysRolePermissionMapper.physicalDeleteByRoleId(roleId);
        Set<Long> distinctPermIds = new LinkedHashSet<>(permIds);
        for (Long permId : distinctPermIds) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermId(permId);
            sysRolePermissionMapper.insert(relation);
        }
        log.info("角色权限分配: roleId={}, 权限数量={}", roleId, distinctPermIds.size());
    }

    /**
     * 校验字符串非空（失败抛IllegalArgumentException，统一映射为400）
     *
     * @param value   字符串值
     * @param message 错误提示
     */
    private void checkNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 校验字符串不超过最大长度（失败抛IllegalArgumentException，统一映射为400）
     *
     * @param value   字符串值
     * @param max     最大长度
     * @param message 错误提示
     */
    private void checkMaxLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(message);
        }
    }
}
