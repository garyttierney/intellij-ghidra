package com.codingmates.ghidra.intellij.ide

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

object GhidraBundle : DynamicBundle("messages.GhidraBundle") {
    fun message(key: @PropertyKey(resourceBundle = "messages.GhidraBundle") String, vararg params: Any): @Nls String {
        return getMessage(key, *params)
    }

    fun messagePointer(
        key: @PropertyKey(resourceBundle = "messages.GhidraBundle") String,
        vararg params: Any
    ): Supplier<String> {
        return getLazyMessage(key, *params)
    }
}
