package rat.poison.game

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import org.jire.kna.attach.Attach
import org.jire.kna.attach.windows.WindowsAttachAccess
import org.jire.kna.attach.windows.WindowsAttachedModule
import org.jire.kna.attach.windows.WindowsAttachedProcess
import org.jire.kna.cached.CachedReadableSource
import rat.poison.curSettings
import rat.poison.dbg
import rat.poison.game.hooks.constructEntities
import rat.poison.game.hooks.updateCursorEnable
import rat.poison.game.netvars.NetVars
import rat.poison.settings.*
import rat.poison.utils.LowPriority
import rat.poison.utils.after
import rat.poison.utils.inBackground
import rat.poison.utils.natives.CUser32
import rat.poison.utils.retry
import kotlin.system.exitProcess

object CSGO {
	const val ENTITY_SIZE = 16
	const val GLOW_OBJECT_SIZE = 56
	val CACHE_EXPIRE_MILLIS = 512L / SERVER_TICK_RATE
	
	lateinit var csgoEXE: WindowsAttachedProcess
		private set
	
	lateinit var clientDLL: WindowsAttachedModule
		private set
	lateinit var engineDLL: WindowsAttachedModule
		private set
	
	var gameHeight: Int = 0
		private set
	
	var gameX: Int = 0
		private set
	
	var gameWidth: Int = 0
		private set
	
	var gameY: Int = 0
		private set
	
	var initialized: Boolean = false
		private set
	
	fun initialize() {
		
		
		after(10000) {
			if (!initialized) {
				println(
					"You are stuck at waiting for ${curSettings["MENU_APP"]}?\n" +
							"Make sure you have checked all of those steps:\n" +
							"- CS:GO is running \n" +
							"- you are running currently most up-to-date version of RatPoison\n" +
							"- you disabled all anti-cheat clients working on your computer\n" +
							"- your RatPoison folder is placed somewhere with all running permissions\n" +
							"- you don't use RatPoison with some other cheats running\n" +
							"- you aren't currently running VAC bypass (running the bat file with administrator privileges should work)\n" +
							"- you restarted your computer\n\n" +
							"If nothing else works then you can try running the bat file as admin."
				)
			}
		}
		
		retry(128) {
			val csgoEXE = Attach.byName(PROCESS_NAME, WindowsAttachAccess(PROCESS_ACCESS_FLAGS)) {
				set(CachedReadableSource.CACHE_EXPIRATION_MILLIS, CACHE_EXPIRE_MILLIS)
			}!!
			CSGO.csgoEXE = csgoEXE as WindowsAttachedProcess
		}
		
		retry(128) {
			val modules = csgoEXE.modules()
			engineDLL = modules.byName(ENGINE_MODULE_NAME) as WindowsAttachedModule
			clientDLL = modules.byName(CLIENT_MODULE_NAME) as WindowsAttachedModule
		}
		initialized = true
		
		val rect = WinDef.RECT()
		val hwd = CUser32.FindWindowA(null, "Counter-Strike: Global Offensive")
		
		//Get initially
		if (!CUser32.GetClientRect(hwd, rect)) exitProcess(2)
		gameWidth = rect.right - rect.left
		gameHeight = rect.bottom - rect.top
		
		if (!CUser32.GetWindowRect(hwd, rect)) exitProcess(3)
		gameX = rect.left + (((rect.right - rect.left) - gameWidth) / 2)
		gameY = rect.top + ((rect.bottom - rect.top) - gameHeight)
		
		LowPriority.every(1000) {
			if (!CUser32.GetClientRect(hwd, rect)) exitProcess(2)
			gameWidth = rect.right - rect.left
			gameHeight = rect.bottom - rect.top
			
			if (!CUser32.GetWindowRect(hwd, rect)) exitProcess(3)
			gameX = rect.left + (((rect.right - rect.left) - gameWidth) / 2)
			gameY = rect.top + ((rect.bottom - rect.top) - gameHeight)
		}
		
		LowPriority.every(1000, continuous = true) {
			inBackground = Pointer.nativeValue(hwd.pointer) != CUser32.GetForegroundWindow()
		}
		
		NetVars.load()
		
		constructEntities()
		updateCursorEnable()
		
		if (dbg) println("[DEBUG] CSGO initialized")
	}
}