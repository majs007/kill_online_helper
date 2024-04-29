package kill.online.helper.utils

import android.content.Context
import androidx.compose.runtime.MutableState


object StateUtils {
    lateinit var applicationContext: Context

    inline fun <reified T> load(
        itemName: FileUtils.ItemName,
        state: MutableState<T>,
        defValue: T,
        context: Context = applicationContext,
    ) {
        state.value = FileUtils.read(
            context = context,
            itemName = itemName,
            defValue = defValue
        )
    }

    inline fun <reified T> load(

        itemName: FileUtils.ItemName,
        state: MutableState<List<T>>,

        defValue: List<T>,
        context: Context = applicationContext,
    ) {
        state.value = FileUtils.read(
            context = context,
            itemName = itemName,
            defValue = defValue
        )
    }

    fun <T> add(

        itemName: FileUtils.ItemName,
        state: MutableState<List<T>>,
        newItem: T,
        autoSave: Boolean = true,
        context: Context = applicationContext,
        callback: (newState: List<T>) -> List<T> = { it }
    ) {
        val newList = state.value.toMutableList()
        newList.add(newItem)
        state.value = callback(newList)
        if (autoSave)
            FileUtils.write(
                context = context,
                itemName = itemName,
                content = state.value
            )
    }

    fun <T> delete(

        itemName: FileUtils.ItemName,
        state: MutableState<List<T>>,
        index: Int,
        autoSave: Boolean = true,
        context: Context = applicationContext,
        callback: (newState: List<T>) -> List<T> = { it }
    ) {
        val newList = state.value.toMutableList()
        newList.removeAt(index)
        state.value = callback(newList)
        if (autoSave)
            FileUtils.write(
                context = context,
                itemName = itemName,
                content = state.value
            )
    }

    fun <T> update(

        itemName: FileUtils.ItemName,
        state: MutableState<List<T>>,
        index: Int,
        autoSave: Boolean = true,
        context: Context = applicationContext,
        handler: (it: T) -> T
    ) {
        val newList = state.value.toMutableList()
        newList[index] = handler(newList[index])
        state.value = newList.toList()
        if (autoSave)
            FileUtils.write(
                context = context,
                itemName = itemName,
                content = state.value
            )
    }


    fun <T> update(
        itemName: FileUtils.ItemName,
        state: MutableState<T>,
        autoSave: Boolean = true,
        context: Context = applicationContext,
        handler: (it: T) -> T
    ) {
        state.value = handler(state.value)
        if (autoSave)
            FileUtils.write(
                context = context,
                itemName = itemName,
                content = state.value
            )
    }

}
