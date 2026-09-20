package com.sky.takeout.controller.user;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.dto.AddressBookUpdateDTO;
import com.sky.takeout.service.AddressBookService;
import com.sky.takeout.vo.AddressBookVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/user/addressBook")
public class AddressBookController {

    private final AddressBookService addressBookService;

    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    @PostMapping
    public Result<AddressBookVO> create(
            @Valid @RequestBody AddressBookCreateDTO createDTO
    ) {
        return Result.success(addressBookService.create(createDTO));
    }

    @GetMapping("/list")
    public Result<List<AddressBookVO>> list() {
        return Result.success(addressBookService.list());
    }

    @GetMapping("/default")
    public Result<AddressBookVO> getDefault() {
        return Result.success(addressBookService.getDefault());
    }

    @GetMapping("/{id}")
    public Result<AddressBookVO> getById(@PathVariable @Positive Long id) {
        return Result.success(addressBookService.getById(id));
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody AddressBookUpdateDTO updateDTO
    ) {
        addressBookService.update(updateDTO);
        return Result.success(null);
    }

    @PutMapping("/default/{id}")
    public Result<Void> setDefault(@PathVariable @Positive Long id) {
        addressBookService.setDefault(id);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive Long id) {
        addressBookService.delete(id);
        return Result.success(null);
    }
}
