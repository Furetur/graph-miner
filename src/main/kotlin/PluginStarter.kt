import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.controlFlow.AllVariablesControlFlowPolicy
import com.intellij.psi.controlFlow.ControlFlow
import com.intellij.psi.controlFlow.ControlFlowFactory
import com.intellij.psi.util.descendantsOfType
import graph.steps.lastReadWrite.LastReadWriteStep
import kotlin.system.exitProcess

object PluginStarter : ApplicationStarter {
    override fun getCommandName(): String = "graph"

    override fun main(args: MutableList<String>) {
        PluginCommand().main(args.drop(1))
        exitProcess(0)
    }
}

class PluginCommand : CliktCommand() {
    private val input by argument("input").file(canBeDir = true, mustExist = true)

    override fun run() {
        val project = ProjectUtil.openOrImport(input.toPath())
        println("Opened $project")
        for (psiFile in project.psiFiles) {
            println("Building graph for file $psiFile")
            processElement(psiFile)
        }
    }

    private fun processElement(psiElement: PsiElement) {
        val graphBuilder = GraphBuilder(psiElement)
        LastReadWriteStep.build(graphBuilder)
        println(graphBuilder.graph())
    }
}

private val Project.psiFiles: List<PsiFile>
    get() {
        val psiManager = PsiManager.getInstance(this)
        return virtualFiles().mapNotNull { psiManager.findFile(it) }
    }

private fun Project.virtualFiles(): List<VirtualFile> =
    ProjectRootManager
        .getInstance(this)
        .contentRoots
        .flatMap { root ->
            VfsUtil.collectChildrenRecursively(root).filter {
                it.extension == "java" && it.canonicalPath != null
            }
        }
