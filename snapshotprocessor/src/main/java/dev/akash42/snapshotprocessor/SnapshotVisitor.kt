package dev.akash42.snapshotprocessor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import java.io.File

class SnapshotVisitor(
    private val logger: KSPLogger,
    private val map: MutableMap<File, MutableSet<String>>
) : KSVisitorVoid() {

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val fileName =
            function.annotations.find { it.shortName.asString() == "Snapshot" }
                ?.arguments?.find { it.name?.asString() == "fileName" }?.value?.toString()

        val composableName = function.annotations.find { it.shortName.asString() == "Snapshot" }
            ?.arguments?.find { it.name?.asString() == "composableName" }?.value?.toString()
        val fileNameToGenerate = fileName ?: return
        val importNameToGenerate = composableName ?: return
        val outputDir =
            File("/Users/akashkamble/AndroidStudioProjects/SnapshotCodeGenerator/app/src/screenshotTest/kotlin")
        outputDir.mkdirs()
        val file = File(outputDir, fileNameToGenerate)
        logger.warn("Generating file at ${file.absolutePath}")

        map.putIfAbsent(file, mutableSetOf())
        val set = map.getValue(file)
        // get imports from functions
        set.apply {
            add("import ${getPackageName(function.containingFile!!)}.$importNameToGenerate")
        }
        set.apply {
            addAll(getImports(function.containingFile!!).map { import ->
                "import $import"
            })
        }

        set.apply {
            add(createFunctionBody(function))
        }
        super.visitFunctionDeclaration(function, data)
    }

    private fun getImports(file: KSFile): List<String> {
        val fileContent = File(file.filePath).readText()
        val importRegex = Regex("^import\\s+([\\w\\.]+)", RegexOption.MULTILINE)
        return importRegex.findAll(fileContent).map { it.groupValues[1] }.toList()
    }

    private fun getPackageName(file: KSFile): String {
        val fileContent = File(file.filePath).readText()
        val packageRegex = Regex("^package\\s+([\\w\\.]+)", RegexOption.MULTILINE)
        return packageRegex.find(fileContent)?.groupValues?.get(1).orEmpty()
    }

    private fun getFunctionBody(file: KSFile, functionName: String): String? {
        val fileContent = File(file.filePath).readText()
        val functionRegex = Regex("fun\\s+$functionName\\s*\\([^)]*\\)\\s*\\{")
        val matchResult = functionRegex.find(fileContent) ?: return null
        val startIndex = matchResult.range.last + 1
        var braceCount = 1
        var endIndex = startIndex
        while (braceCount > 0 && endIndex < fileContent.length) {
            when (fileContent[endIndex]) {
                '{' -> braceCount++
                '}' -> braceCount--
            }
            endIndex++
        }
        return fileContent.substring(startIndex, endIndex - 1).trim()
    }

    private fun createFunctionBody(function: KSFunctionDeclaration): String {
        val stringBuilder = StringBuilder()
        val functionBody = getFunctionBody(
            function.containingFile!!,
            function.simpleName.asString()
        ).orEmpty()
        if (functionBody.isNotEmpty()) {
            stringBuilder.append("\n")
            stringBuilder.append("@Preview()\n")
            stringBuilder.append("@Composable\n")
            stringBuilder.append("fun ${function.simpleName.asString()}() {\n")
            stringBuilder.append("  $functionBody")
            stringBuilder.append("\n")
            stringBuilder.append("}\n\n")
        }
        return stringBuilder.toString()
    }
}