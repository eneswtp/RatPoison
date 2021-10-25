package rat.poison.ui.uiWindows

import com.badlogic.gdx.graphics.g2d.Batch
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisSlider
import com.kotcrab.vis.ui.widget.VisWindow
import rat.poison.curSettings
import rat.poison.overlay.App.uiSpecList
import rat.poison.overlay.opened
import rat.poison.ui.changed
import rat.poison.ui.uiUpdate
import kotlin.math.round

lateinit var specListText : VisLabel

//Needs cleanup

class UISpectatorList : VisWindow("Spectator List") {
    private val menuAlphaSlider = VisSlider(0.5F, 1F, 0.05F, true)

    init {
        defaults().left()
        addCloseButton()

        specListText = VisLabel()

        //Create UI_Alpha Slider
        menuAlphaSlider.value = curSettings.float["SPECTATOR_LIST_ALPHA"]
        updateAlpha()
        menuAlphaSlider.changed { _, _ ->
            val alp = (round(menuAlphaSlider.value * 100F) / 100F)
            curSettings["SPECTATOR_LIST_ALPHA"] = alp
            updateAlpha()

            true
        }

        columnDefaults(2)

        add(specListText).growX().fillX().expandX().expandY().center().top().colspan(1)
        add(menuAlphaSlider).colspan(1).growY()

        pack()

        setSize(300F, 350F)
        setPosition(curSettings.float["SPECTATOR_LIST_X"], curSettings.float["SPECTATOR_LIST_Y"])
        isResizable = false
    }

    override fun positionChanged() {
        if (opened) {
            curSettings["SPECTATOR_LIST_X"] = uiSpecList.x
            curSettings["SPECTATOR_LIST_Y"] = uiSpecList.y
            super.positionChanged()
        }
    }

    override fun close() {
        curSettings["SPECTATOR_LIST"] = "false"
        uiUpdate()
    }

    fun updateAlpha() {
        color.a = curSettings.float["SPECTATOR_LIST_ALPHA"]
        menuAlphaSlider.color.a = color.a
    }

    override fun drawChildren(batch: Batch?, parentAlpha: Float) {
        super.drawChildren(batch, 1/color.a)
    }

    fun updatePosition(x: Float, y: Float) {
        setPosition(x, y)
    }
}