package com.carmaintenance.controller;

import com.carmaintenance.common.Result;
import com.carmaintenance.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dicts")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;

    @GetMapping
    public Result<?> listAll() {
        return Result.success(dictService.list());
    }

    @GetMapping("/{dictCode}/items")
    public Result<?> getItems(@PathVariable String dictCode) {
        return Result.success(dictService.getItemsByDictCode(dictCode));
    }
}
