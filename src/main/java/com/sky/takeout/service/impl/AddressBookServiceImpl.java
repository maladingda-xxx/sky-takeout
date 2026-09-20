package com.sky.takeout.service.impl;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.dto.AddressBookUpdateDTO;
import com.sky.takeout.entity.AddressBook;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.AddressBookMapper;
import com.sky.takeout.service.AddressBookService;
import com.sky.takeout.vo.AddressBookVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    private static final int DEFAULT_ADDRESS = 1;
    private static final int NORMAL_ADDRESS = 0;

    private final AddressBookMapper addressBookMapper;

    public AddressBookServiceImpl(AddressBookMapper addressBookMapper) {
        this.addressBookMapper = addressBookMapper;
    }

    @Override
    @Transactional
    public AddressBookVO create(AddressBookCreateDTO createDTO) {
        Long userId = UserContext.getRequiredUserId();
        boolean firstAddress = addressBookMapper.countByUserId(userId) == 0;
        boolean shouldBeDefault = firstAddress
                || Integer.valueOf(DEFAULT_ADDRESS).equals(createDTO.getIsDefault());

        if (shouldBeDefault) {
            addressBookMapper.clearDefaultByUserId(userId);
        }

        AddressBook address = new AddressBook();
        copyCreateFields(address, createDTO);
        address.setUserId(userId);
        address.setIsDefault(shouldBeDefault ? DEFAULT_ADDRESS : NORMAL_ADDRESS);

        if (addressBookMapper.insert(address) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create address"
            );
        }

        return toVO(address);
    }

    @Override
    public List<AddressBookVO> list() {
        return addressBookMapper.selectByUserId(UserContext.getRequiredUserId())
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public AddressBookVO getById(Long id) {
        return toVO(requireAddress(id));
    }

    @Override
    public AddressBookVO getDefault() {
        AddressBook address = addressBookMapper.selectDefaultByUserId(
                UserContext.getRequiredUserId()
        );
        return address == null ? null : toVO(address);
    }

    @Override
    @Transactional
    public void update(AddressBookUpdateDTO updateDTO) {
        Long userId = UserContext.getRequiredUserId();
        requireAddress(updateDTO.getId());

        if (Integer.valueOf(DEFAULT_ADDRESS).equals(updateDTO.getIsDefault())) {
            addressBookMapper.clearDefaultByUserId(userId);
        }

        AddressBook address = new AddressBook();
        copyUpdateFields(address, updateDTO);
        address.setId(updateDTO.getId());
        address.setUserId(userId);
        address.setIsDefault(
                Integer.valueOf(DEFAULT_ADDRESS).equals(updateDTO.getIsDefault())
                        ? DEFAULT_ADDRESS
                        : NORMAL_ADDRESS
        );

        if (addressBookMapper.update(address) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to update address"
            );
        }
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        Long userId = UserContext.getRequiredUserId();
        requireAddress(id);

        addressBookMapper.clearDefaultByUserId(userId);
        if (addressBookMapper.setDefault(id, userId) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to set default address"
            );
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = UserContext.getRequiredUserId();
        AddressBook existing = requireAddress(id);

        if (addressBookMapper.deleteByIdAndUserId(id, userId) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete address"
            );
        }

        if (Integer.valueOf(DEFAULT_ADDRESS).equals(existing.getIsDefault())) {
            AddressBook next = addressBookMapper.selectFirstByUserId(userId);
            if (next != null) {
                addressBookMapper.setDefault(next.getId(), userId);
            }
        }
    }

    private AddressBook requireAddress(Long id) {
        AddressBook address = addressBookMapper.selectByIdAndUserId(
                id,
                UserContext.getRequiredUserId()
        );
        if (address == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Address not found"
            );
        }
        return address;
    }

    private void copyCreateFields(
            AddressBook address,
            AddressBookCreateDTO createDTO
    ) {
        address.setConsignee(createDTO.getConsignee().trim());
        address.setSex(createDTO.getSex());
        address.setPhone(createDTO.getPhone());
        address.setProvinceName(createDTO.getProvinceName().trim());
        address.setCityName(createDTO.getCityName().trim());
        address.setDistrictName(createDTO.getDistrictName().trim());
        address.setDetail(createDTO.getDetail().trim());
        address.setLabel(normalizeLabel(createDTO.getLabel()));
    }

    private void copyUpdateFields(
            AddressBook address,
            AddressBookUpdateDTO updateDTO
    ) {
        address.setConsignee(updateDTO.getConsignee().trim());
        address.setSex(updateDTO.getSex());
        address.setPhone(updateDTO.getPhone());
        address.setProvinceName(updateDTO.getProvinceName().trim());
        address.setCityName(updateDTO.getCityName().trim());
        address.setDistrictName(updateDTO.getDistrictName().trim());
        address.setDetail(updateDTO.getDetail().trim());
        address.setLabel(normalizeLabel(updateDTO.getLabel()));
    }

    private String normalizeLabel(String label) {
        if (label == null) {
            return null;
        }
        String normalized = label.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private AddressBookVO toVO(AddressBook address) {
        return new AddressBookVO(
                address.getId(),
                address.getConsignee(),
                address.getSex(),
                address.getPhone(),
                address.getProvinceName(),
                address.getCityName(),
                address.getDistrictName(),
                address.getDetail(),
                address.getLabel(),
                address.getIsDefault()
        );
    }
}
