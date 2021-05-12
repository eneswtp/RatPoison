package rat.poison.ui.uiElements

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.Tooltip
import com.kotcrab.vis.ui.widget.VisSelectBox
import com.kotcrab.vis.ui.widget.VisTable
import rat.poison.curSettings
import rat.poison.dbg
import rat.poison.scripts.aim.boneToNum
import rat.poison.scripts.aim.numToBone
import rat.poison.scripts.aim.stringToBoneList
import rat.poison.ui.changed
import rat.poison.ui.uiTabs.VisLabelCustom
import rat.poison.ui.uiTabs.categorySelected
import rat.poison.ui.uiTabs.updateDisableRCrosshair
import rat.poison.utils.extensions.upper
import rat.poison.utils.locale

class VisComboBoxCustom(mainText: String, varName: String, useCategory: Boolean, showText: Boolean = true, vararg items: String, textWidth: Float = 200F, boxWidth: Float = 100F): VisTable(false) {
    private val textLabel = mainText
    private val variableName = varName
    private val useGunCategory = useCategory
    private var hasTooltip = false

    private var dropDownWidth = boxWidth

    private var boxLabel = VisLabelCustom("$textLabel:")
    private val selectBox = VisSelectBox<String>()

    private val boxItems = items

    var selectedItems = mutableListOf<Int>()

    init {
        val curValue = curSettings[if (useGunCategory) { categorySelected + variableName } else { variableName }].stringToBoneList()

        curValue.forEach {
            selectedItems.add(boxItems.indexOf(it.numToBone())+1)
        }

        update()
        updateTooltip()
        updateList()

        selectBox.changed { _, _ ->
            val idx = selectBox.selectedIndex

            if (idx == 0) {
                //Do nothin
            } else {
                if (selectedItems.contains(idx)) {
                    selectedItems.remove(idx)
                    updateList()
                } else {
                    selectedItems.add(idx)
                    updateList()
                }
            }

            val strItems = mutableListOf<String>()
            for (i in selectedItems) {
                strItems.add(boxItems[i-1])
            }

            curSettings[if (useGunCategory) { categorySelected + variableName } else { variableName }] = strItems

            false
        }

        if (showText) {
            add(boxLabel).width(textWidth)
        }

        add(selectBox).width(dropDownWidth)
    }

    fun updateList() {
        val itemsArray = Array<String>()

        var str = ""
        for (i in selectedItems) {
            str += boxItems[i-1] + ", "
        }

        itemsArray.add(str)

        for (i in boxItems) {
            itemsArray.add("L_$i".locale(i))
        }
        selectBox.items = itemsArray
        selectBox.selectedIndex = 0
    }

    fun update() {
        val setting = if (useGunCategory) { categorySelected + variableName } else { variableName }

        try {
            selectBox.selectedIndex = boxItems.indexOf(curSettings[setting].upper())
        } catch (e: Exception) {
            if (selectBox.items.size > 0) {
                selectBox.selectedIndex = 0
            }

            if (dbg) {
                println("[DEBUG - Error Handling] -- $setting invalid, setting value to [${selectBox.selected}]")
            }
        }

        boxLabel.setText("L$variableName".locale(textLabel))

        updateTooltip()
    }

    private fun updateTooltip() {
        if (curSettings.bool["MENU_TOOLTIPS"]) {
            //TODO tooltips
        } else {
            if (hasTooltip) {
                Tooltip.removeTooltip(this)
                hasTooltip = false
            }
        }
    }

    fun disable(bool: Boolean, col: Color) {
        boxLabel.color = col
        selectBox.color = col
        selectBox.isDisabled = bool
    }
}