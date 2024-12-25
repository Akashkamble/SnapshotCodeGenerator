package dev.akash42.snapshotprocessor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import dev.akash42.snapshotannotation.Snapshot
import java.io.File

class SnapshotProcessor(
    private val logger: KSPLogger,
) : SymbolProcessor {
    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("Hooray!!!")
        val symbols = resolver.getSymbolsWithAnnotation(Snapshot::class.qualifiedName!!)
        val validSymbols = symbols.filter { it is KSFunctionDeclaration && it.validate() }
            .filter { function ->
                val annotationNames = function.annotations.map { it.shortName.asString() }
                "Preview" in annotationNames && "Composable" in annotationNames
            }
            .map { it as KSFunctionDeclaration }

        val map = mutableMapOf<File, MutableSet<String>>()

        validSymbols.toList().forEach {
            it.accept(SnapshotVisitor(logger, map), Unit)
        }

        map.forEach { (file, set) ->
            file.bufferedWriter().use { writer ->
                set.forEach {
                    writer.write(it)
                    writer.newLine()
                }
            }
        }

        return emptyList()
    }
}