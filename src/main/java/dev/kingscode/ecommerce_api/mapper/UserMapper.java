package dev.kingscode.ecommerce_api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import dev.kingscode.ecommerce_api.dto.user.request.CreateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.UpdateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    /**
     * Create User entity from register user DTO
     * NOTE:
     * - password is mapped but should be ENCODED in service
     * - role is set in service (security decision)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "pwdResetToken", ignore = true)
    @Mapping(target = "tokenExpiration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(CreateUserRequestDto dto);

    /**
     * Update mutable user fields
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "pwdResetToken", ignore = true)
    @Mapping(target = "tokenExpiration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateUserRequestDto dto, @MappingTarget User user);

    UserResponseDto toResponseDto(User user);

}
