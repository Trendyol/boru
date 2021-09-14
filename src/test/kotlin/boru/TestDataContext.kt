package com.trendyol.boru

class TestDataContext : PipelineContext {
    override val items: MutableMap<Any, Any> = mutableMapOf()

    var intValue: Int = 0

    var text: String?
        get() = items.getOrDefault("Text", null).toString()
        set(value) {
            items["Text"] = value!!
        }
}