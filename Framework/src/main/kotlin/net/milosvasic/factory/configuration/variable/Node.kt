package net.milosvasic.factory.configuration.variable

import com.google.gson.*
import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.GsonDeserialization
import java.lang.reflect.Type

data class Node(
        val name: String = String.EMPTY,
        val value: Any = String.EMPTY,
        val children: MutableList<Node> = mutableListOf()
) {

    companion object : GsonDeserialization<Node> {

        const val CONTEXT_SEPARATOR = "."

        override fun getDeserializer(): JsonDeserializer<Node> {
            return object : JsonDeserializer<Node> {

                override fun deserialize(
                        json: JsonElement?,
                        typeOfT: Type?,
                        context: JsonDeserializationContext?
                ): Node {

                    if (json == null) {
                        throw JsonParseException("JSON is null")
                    }
                    when (json) {
                        is JsonObject -> {
                            val name = String.EMPTY
                            val entrySet = json.entrySet()
                            val children = mutableListOf<Node>()
                            entrySet.forEach { item ->

                                val itemName = item.key
                                val itemValue = item.value
                                if (itemName == String.EMPTY) {
                                    throw JsonParseException("Empty key")
                                }
                                when (item.value) {
                                    is JsonObject -> {
                                        val itemObjectValue = itemValue.asJsonObject
                                        val child = processJsonObject(itemName, itemObjectValue)
                                        children.add(child)
                                    }
                                    is JsonPrimitive -> {
                                        val itemPrimitiveValue = itemValue.asJsonPrimitive
                                        val value: Any = when {
                                            itemPrimitiveValue.isBoolean -> {
                                                itemValue.asBoolean
                                            }
                                            itemPrimitiveValue.isNumber -> {

                                                if (itemValue.toString().contains(".")) {
                                                    itemValue.asFloat
                                                } else {
                                                    itemValue.asInt
                                                }
                                            }
                                            else -> {
                                                itemValue.asString
                                            }
                                        }
                                        val child = Node(
                                                name = itemName,
                                                value = value
                                        )
                                        children.add(child)
                                    }
                                }
                            }
                            return Node(
                                    name = name,
                                    children = children
                            )
                        }
                        else -> {
                            throw JsonParseException("Unexpected JSON element: ${json::class.simpleName}")
                        }
                    }
                }
            }
        }

        @Throws(JsonParseException::class)
        private fun processJsonObject(parent: String, jsonObject: JsonObject): Node {
            if (jsonObject.keySet().isEmpty()) {
                throw JsonParseException("No keys")
            } else {
                val entrySet = jsonObject.entrySet()
                val children = mutableListOf<Node>()
                entrySet.forEach { item ->
                    val itemKey = item.key
                    when (val itemValue = item.value) {
                        is JsonObject -> {
                            val child = processJsonObject(itemKey, itemValue)
                            children.add(child)
                        }
                        is JsonPrimitive -> {
                            var value: Any = String.EMPTY
                            when {
                                itemValue.isString -> {
                                    value = itemValue.asString
                                }
                                itemValue.isBoolean -> {
                                    value = itemValue.asBoolean
                                }
                                itemValue.isNumber -> {

                                    value = if (itemValue.toString().contains(".")) {
                                        itemValue.asFloat
                                    } else {
                                        itemValue.asInt
                                    }
                                }
                            }
                            val child = Node(
                                    name = itemKey,
                                    value = value
                            )
                            children.add(child)
                        }
                        else -> {
                            throw JsonParseException("Unsupported structure member: ${itemValue::class.simpleName}")
                        }
                    }
                }
                return Node(
                        name = parent,
                        children = children
                )
            }
        }
    }

    fun append(vararg nodes: Node): Node {

        nodes.forEach { node ->
            if (node.children.isNotEmpty()) {
                if (node.name != String.EMPTY) {
                    var added = false
                    children.forEach { child ->
                        if (child.name == node.name) {
                            node.children.forEach { nodeChild ->
                                child.append(nodeChild)
                            }
                            added = true
                        }
                    }
                    if (!added) {
                        children.add(node)
                    }
                } else {
                    append(*node.children.toTypedArray())
                }
            }
            if (node.value != String.EMPTY) {
                children.add(node)
            }
        }
        return this
    }

    fun get(what: String): Any? {

        val path = what.split(CONTEXT_SEPARATOR)
        val iterator = path.iterator()
        var node = this
        while (iterator.hasNext()) {
            val position = iterator.next()
            node.children.forEach { child ->
                if (child.name == position.trim()) {
                    node = child
                }
            }
            if (node == this) {
                return null
            }
        }
        return node.value
    }
}