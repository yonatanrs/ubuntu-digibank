package com.ubuntu.bank.payment.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.ubuntu.bank.payment", importOptions = ImportOption.DoNotIncludeTests.class)
class PaymentArchitectureTest {
    @ArchTest
    static final ArchRule layersAreRespected = layeredArchitecture().consideringOnlyDependenciesInLayers()
        .layer("API").definedBy("..api..")
        .layer("Application").definedBy("..application..")
        .layer("Domain").definedBy("..domain..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .whereLayer("API").mayNotBeAccessedByAnyLayer()
        .whereLayer("Application").mayOnlyBeAccessedByLayers("API", "Infrastructure")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("API", "Application", "Infrastructure")
        .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application");
}

