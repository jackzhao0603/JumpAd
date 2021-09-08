package com.jackzhao.adjump.config

import com.jackzhao.simple_kv.IBaseKv

enum class Config(var defaultValue: Any) : IBaseKv {
    IS_JUMP_ENABLE(true);
}