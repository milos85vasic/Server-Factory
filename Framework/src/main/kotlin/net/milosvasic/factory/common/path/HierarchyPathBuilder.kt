package net.milosvasic.factory.common.path

abstract class HierarchyPathBuilder<T, CTX, KEY, SEP> : PathBuilder<T, CTX, SEP>() {

    protected var key: KEY? = null

    fun setKey(key: KEY): HierarchyPathBuilder<T, CTX, KEY, SEP> {
        this.key = key
        return this
    }

    fun hasKey() = key != null

    override fun addContext(context: CTX): HierarchyPathBuilder<T, CTX, KEY, SEP> {
        super.addContext(context)
        return this
    }
}