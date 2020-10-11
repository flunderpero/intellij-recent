package flunderpero.recent_files_improved

import com.intellij.ide.actions.GotoActionBase
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NotNull
import javax.swing.Icon

class MainAction : GotoActionBase(), DumbAware {
    override fun gotoActionPerformed(e: AnActionEvent) {
        val project = e.getData(PROJECT) ?: return
        showNavigationPopup(e, MyModel(project), GoToFile(), false)
    }

    private class GoToFile : GotoActionBase.GotoActionCallback<String>() {
        override fun elementChosen(popup: ChooseByNamePopup, element: Any) {
            if (element is flunderpero.recent_files_improved.NavigationItem) {
                element.navigate(true)
            }
        }
    }
}

class MyModel(project: Project) :
    FilteringGotoByModel<FileType>(project, arrayOf(NavigateToModelContributor())), DumbAware {
    override fun willOpenEditor(): Boolean = false
    override fun saveInitialCheckBoxState(state: Boolean) = Unit
    override fun getFullName(element: Any): String? = getElementName(element)
    override fun loadInitialCheckBoxState(): Boolean = false
    override fun getPromptText(): String = "Recent Files"
    override fun getNotInMessage(): String = "No matches found"
    override fun getCheckBoxName(): String? = null
    override fun getSeparators(): Array<String> = emptyArray()
    override fun filterValueFor(item: NavigationItem): FileType? = null
    override fun getNotFoundMessage(): String = "File not found"
}

class NavigateToModelContributor(
    private var navigationItems: List<flunderpero.recent_files_improved.NavigationItem> = emptyList()) :
    ChooseByNameContributor {
    override fun getNames(project: Project, includeNonProjectItems: Boolean): Array<out @NotNull String> {
        navigationItems = EditorHistoryManager.getInstance(project).fileList.map { x ->
            NavigationItem(x, project)
        }
        return navigationItems.map { item -> item.name }.sorted().toTypedArray()
    }

    override fun getItemsByName(name: String?, pattern: String?, project: Project?,
        includeNonProjectItems: Boolean): Array<flunderpero.recent_files_improved.NavigationItem> {
        return navigationItems.filter { item -> item.name == name }.sortedBy { name }.toTypedArray()
    }

}

class NavigationItem(private val file: VirtualFile, private val project: Project) : NavigationItem {
    override fun navigate(requestFocus: Boolean) {
        FileEditorManager.getInstance(project).openFile(file, requestFocus)
    }

    override fun getPresentation(): ItemPresentation? = RequestMappingItemPresentation()
    override fun canNavigate(): Boolean = true
    override fun getName(): String = file.name
    override fun canNavigateToSource(): Boolean = false
    private inner class RequestMappingItemPresentation : ItemPresentation {
        override fun getPresentableText() = file.name
        override fun getLocationString(): String? = null
        override fun getIcon(b: Boolean): Icon? = null
    }
}
