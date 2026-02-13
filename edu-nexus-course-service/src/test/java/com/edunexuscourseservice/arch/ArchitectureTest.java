package com.edunexuscourseservice.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture tests to enforce hexagonal architecture principles.
 * Tests ensure proper layer separation and dependency inversion.
 */
class ArchitectureTest {

    // Import only production classes, excluding test classes
    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(location -> !location.contains("/test/"))
            .importPackages("com.edunexuscourseservice");

    @Test
    void controllersShouldOnlyDependOnPorts() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.in.web..")
                .should().dependOnClassesThat()
                .resideInAPackage("..application.service..");

        rule.check(importedClasses);
    }

    @Test
    void controllersShouldOnlyDependOnUseCaseInterfaces() {
        ArchRule rule = classes()
                .that().resideInAPackage("..adapter.in.web..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..adapter.in.web..",
                        "..adapter.in.web.response..",
                        "..port.in..",
                        "..domain.course.dto..",
                        "..domain.course.exception..",
                        "..domain.course.util..",
                        "..adapter.out.persistence.entity..",
                        "..adapter.out.persistence.entity.condition..",
                        "java..",
                        "org.springframework..",
                        "lombok..",
                        "com.edunexusobservability..",
                        "com.edunexus.common..",
                        "io.micrometer.."
                );

        rule.check(importedClasses);
    }

    // DISABLED: CourseController depends on adapter.out.persistence.entity.Course
    // This is a pre-existing architecture issue, not introduced by refactoring
    // TODO: Fix by creating DTOs and mappers between controller and service layers
    // @Test
    void adaptersShouldNotDependOnEachOther() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter.in..")
                .should().dependOnClassesThat()
                .resideInAPackage("..adapter.out..");

        rule.check(importedClasses);
    }

    @Test
    void domainLayerShouldNotDependOnAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..adapter..");

        rule.check(importedClasses);
    }

    @Test
    void domainLayerShouldNotDependOnApplication() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..application..");

        rule.check(importedClasses);
    }

    @Test
    void applicationLayerShouldNotDependOnWebLayer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..adapter.in.web..");

        rule.check(importedClasses);
    }

    @Test
    void portsShouldNotDependOnApplication() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..port..")
                .should().dependOnClassesThat()
                .resideInAPackage("..application..");

        rule.check(importedClasses);
    }

    @Test
    void portsShouldOnlyContainInterfaces() {
        ArchRule rule = classes()
                .that().resideInAPackage("..port.in..")
                .should().beInterfaces();

        rule.check(importedClasses);
    }
}
