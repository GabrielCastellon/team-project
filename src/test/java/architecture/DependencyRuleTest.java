package architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Enforces inward dependencies as teammates add real feature classes. */
class DependencyRuleTest {
    @Test
    void layersDoNotDependOnOuterImplementations() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("app", "entity", "use_case", "interface_adapter", "data_access", "view");
        noClasses().that().resideInAPackage("entity..")
                .should().dependOnClassesThat().resideOutsideOfPackages("entity..", "java..")
                .allowEmptyShould(true).check(classes);
        noClasses().that().resideInAPackage("use_case..")
                .should().dependOnClassesThat()
                .resideOutsideOfPackages("use_case..", "entity..", "java..")
                .allowEmptyShould(true).check(classes);
        noClasses().that().resideInAPackage("interface_adapter..")
                .should().dependOnClassesThat().resideInAnyPackage("app..", "view..", "data_access..")
                .allowEmptyShould(true).check(classes);
        noClasses().that().resideInAPackage("data_access..")
                .should().dependOnClassesThat().resideInAnyPackage("app..", "view..", "interface_adapter..")
                .allowEmptyShould(true).check(classes);
        noClasses().that().resideInAPackage("view..")
                .should().dependOnClassesThat().resideInAnyPackage("app..", "data_access..", "use_case..")
                .allowEmptyShould(true).check(classes);
    }
}
