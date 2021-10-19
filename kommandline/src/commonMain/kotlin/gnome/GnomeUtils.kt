@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.Platform
import pl.mareklangiewicz.kommand.bash
import pl.mareklangiewicz.kommand.line
import pl.mareklangiewicz.kommand.zenityAskIf


fun Platform.startInGnomeTermIfUserConfirms(
    kommand: Kommand,
    confirmation: String = "Run ::${kommand.line()}:: in gnome terminal?",
    title: String = kommand.name,
    insideBash: Boolean = true,
    pauseBeforeExit: Boolean = insideBash,
    execInDir: String? = null
) {
    if (zenityAskIf(confirmation, title)) {
        val k = when {
            insideBash -> bash(kommand, pauseBeforeExit)
            pauseBeforeExit -> error("Can not pause before exit if not using bash shell")
            else -> kommand
        }
        start(gnometerm(k), execInDir)
    }
}


// TODO: command: dbus-send
// for example to be able to clear notifications in optimal ways in comparison to:
// dbus-send --session --type=method_call --dest=org.gnome.Shell /org/gnome/Shell org.gnome.Shell.Eval string:'Main.panel.statusArea.dateMenu._messageList._sectionList.get_children().forEach(s => s.clear());'
// TODO: also: analyze and implement Kommands for stuff like:
// xgettext --output=locale/example.pot *.js (https://www.codeproject.com/Articles/5271677/How-to-Create-A-GNOME-Extension)
// msginit --locale fr --input locale/example.pot --output
// msgfmt example.po --output-file=example.mo
// dconf-editor
// glib-compile-schemas schemas/